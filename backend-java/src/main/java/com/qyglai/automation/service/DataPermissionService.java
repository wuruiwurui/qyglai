package com.qyglai.automation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.qyglai.automation.entity.SysOrgEntity;
import com.qyglai.automation.entity.SysRoleEntity;
import com.qyglai.automation.entity.SysUserEntity;
import com.qyglai.automation.entity.SysUserRoleEntity;
import com.qyglai.automation.mapper.SysOrgMapper;
import com.qyglai.automation.mapper.SysRoleMapper;
import com.qyglai.automation.mapper.SysUserMapper;
import com.qyglai.automation.mapper.SysUserRoleMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 数据权限服务，统一计算当前用户能查看的数据范围。
 */
@Service
public class DataPermissionService {

    private static final String SCOPE_ALL = "all";
    private static final String SCOPE_DEPT_TREE = "dept_tree";
    private static final String SCOPE_DEPT_AND_CHILD = "dept_and_child";
    private static final String SCOPE_DEPT = "dept";
    private static final String SCOPE_SELF = "self";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrgMapper orgMapper;

    public DataPermissionService(SysUserMapper userMapper, SysRoleMapper roleMapper,
                                 SysUserRoleMapper userRoleMapper, SysOrgMapper orgMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.orgMapper = orgMapper;
    }

    /** 查询用户所属组织。 */
    public Long userOrgId(Long userId) {
        SysUserEntity user = userId == null ? null : userMapper.selectById(userId);
        return user == null ? null : user.getOrgId();
    }

    /** 根据用户角色计算最终数据范围，多个角色取权限最大的范围。 */
    public String dataScope(Long userId, boolean apiViewAll) {
        if (apiViewAll) {
            return SCOPE_ALL;
        }
        List<Long> roleIds = userRoleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRoleEntity>()
                        .eq(SysUserRoleEntity::getUserId, userId))
                .stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return SCOPE_SELF;
        }
        List<String> scopes = roleMapper.selectBatchIds(roleIds).stream().map(SysRoleEntity::getDataScope).toList();
        if (scopes.stream().anyMatch(SCOPE_ALL::equalsIgnoreCase)) return SCOPE_ALL;
        if (scopes.stream().anyMatch(scope -> SCOPE_DEPT_TREE.equalsIgnoreCase(scope) || SCOPE_DEPT_AND_CHILD.equalsIgnoreCase(scope))) {
            return SCOPE_DEPT_TREE;
        }
        if (scopes.stream().anyMatch(SCOPE_DEPT::equalsIgnoreCase)) return SCOPE_DEPT;
        return SCOPE_SELF;
    }

    /** 查询本部门及下级组织ID。 */
    public List<Long> permittedOrgIds(Long userId, boolean includeChildren) {
        Long rootOrgId = userOrgId(userId);
        if (rootOrgId == null) {
            return List.of();
        }
        if (!includeChildren) {
            return List.of(rootOrgId);
        }
        List<SysOrgEntity> orgs = orgMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysOrgEntity>()
                .eq(SysOrgEntity::getStatus, "enabled"));
        Set<Long> result = new HashSet<>();
        collectChildren(rootOrgId, orgs, result);
        return new ArrayList<>(result);
    }

    /** 对同时拥有负责人和组织字段的业务表应用数据权限。 */
    public <T> LambdaQueryWrapper<T> applyOwnerOrgScope(LambdaQueryWrapper<T> query,
                                                        SFunction<T, ?> ownerColumn,
                                                        SFunction<T, ?> orgColumn,
                                                        Long userId,
                                                        boolean apiViewAll) {
        String scope = dataScope(userId, apiViewAll);
        if (SCOPE_ALL.equals(scope)) return query;
        if (SCOPE_SELF.equals(scope)) return query.eq(ownerColumn, userId);
        boolean tree = SCOPE_DEPT_TREE.equals(scope);
        List<Long> orgIds = permittedOrgIds(userId, tree);
        if (orgIds.isEmpty()) return query.eq(ownerColumn, userId);
        return query.and(wrapper -> wrapper.in(orgColumn, orgIds).or().eq(ownerColumn, userId));
    }

    /** 对只有负责人字段的业务表应用数据权限。 */
    public <T> LambdaQueryWrapper<T> applyOwnerScope(LambdaQueryWrapper<T> query,
                                                     SFunction<T, ?> ownerColumn,
                                                     Long userId,
                                                     boolean apiViewAll) {
        if (SCOPE_ALL.equals(dataScope(userId, apiViewAll))) return query;
        return query.eq(ownerColumn, userId);
    }

    /** 对只有组织字段的业务表应用数据权限。 */
    public <T> LambdaQueryWrapper<T> applyOrgScope(LambdaQueryWrapper<T> query,
                                                   SFunction<T, ?> orgColumn,
                                                   Long userId,
                                                   boolean apiViewAll) {
        String scope = dataScope(userId, apiViewAll);
        if (SCOPE_ALL.equals(scope)) return query;
        List<Long> orgIds = permittedOrgIds(userId, SCOPE_DEPT_TREE.equals(scope));
        return orgIds.isEmpty() ? query.eq(orgColumn, -1L) : query.in(orgColumn, orgIds);
    }

    private void collectChildren(Long currentOrgId, List<SysOrgEntity> orgs, Set<Long> result) {
        result.add(currentOrgId);
        for (SysOrgEntity org : orgs) {
            if (currentOrgId.equals(org.getParentId()) && result.add(org.getId())) {
                collectChildren(org.getId(), orgs, result);
            }
        }
    }
}
