<template>
  <section class="permission-workbench">
    <section class="permission-summary">
      <article><span>组织</span><strong>{{ orgs.length }}</strong></article>
      <article><span>用户</span><strong>{{ users.length }}</strong></article>
      <article><span>角色</span><strong>{{ roles.length }}</strong></article>
      <article><span>权限</span><strong>{{ permissions.length }}</strong></article>
    </section>

    <div class="permission-tabs">
      <button v-for="item in tabs" :key="item.key" :class="{ active: tab === item.key }" type="button" @click="tab = item.key">{{ item.label }}</button>
      <button class="ghost-button" type="button" @click="loadAll"><RefreshCw :size="15" />刷新</button>
    </div>

    <section class="permission-layout">
      <section class="permission-list">
        <div class="file-section-head">
          <div><p>系统权限</p><h2>{{ currentTitle }}</h2></div>
          <button class="submit-action" type="button" @click="startCreate"><Plus :size="15" />新增</button>
        </div>
        <div class="permission-items">
          <button v-for="item in currentItems" :key="String(item.id)" :class="{ active: selectedId === String(item.id) }" type="button" @click="selectItem(item)">
            <strong>{{ itemName(item) }}</strong>
            <small>{{ itemCode(item) }} · {{ statusLabel(String(item.status ?? 'enabled')) }}</small>
          </button>
          <div v-if="!currentItems.length" class="file-empty">暂无数据</div>
        </div>
      </section>

      <section class="permission-editor">
        <div class="file-section-head">
          <div><p>{{ selectedId ? "编辑" : "新增" }}</p><h2>{{ currentTitle }}</h2></div>
          <span class="status-chip">{{ notice }}</span>
        </div>

        <form class="permission-form" @submit.prevent="saveCurrent">
          <template v-if="tab === 'orgs'">
            <label><span>组织编码</span><input v-model="form.orgCode" required /></label>
            <label><span>组织名称</span><input v-model="form.orgName" required /></label>
            <label><span>父级组织</span><select v-model="form.parentId"><option value="0">无</option><option v-for="org in orgs" :key="org.id" :value="org.id">{{ org.orgName }}</option></select></label>
            <label><span>组织类型</span><select v-model="form.orgType"><option value="company">公司</option><option value="department">部门</option><option value="team">团队</option></select></label>
          </template>
          <template v-if="tab === 'users'">
            <label><span>用户名</span><input v-model="form.username" required /></label>
            <label><span>真实姓名</span><input v-model="form.realName" required /></label>
            <label><span>所属组织</span><select v-model="form.orgId" required><option v-for="org in orgs" :key="org.id" :value="org.id">{{ org.orgName }}</option></select></label>
            <label><span>密码</span><input v-model="form.password" type="password" placeholder="编辑时留空表示不修改" /></label>
            <label><span>手机号</span><input v-model="form.mobile" /></label>
            <label><span>邮箱</span><input v-model="form.email" /></label>
          </template>
          <template v-if="tab === 'roles'">
            <label><span>角色编码</span><input v-model="form.roleCode" required /></label>
            <label><span>角色名称</span><input v-model="form.roleName" required /></label>
            <label><span>数据范围</span><select v-model="form.dataScope"><option value="all">全部数据</option><option value="org">本部门</option><option value="org_and_children">本部门及下级</option><option value="self">仅本人</option></select></label>
          </template>
          <template v-if="tab === 'permissions'">
            <label><span>权限编码</span><input v-model="form.permissionCode" required placeholder="例如 system:manage" /></label>
            <label><span>权限名称</span><input v-model="form.permissionName" required /></label>
            <label><span>权限类型</span><select v-model="form.permissionType"><option value="menu">菜单</option><option value="button">按钮</option><option value="api">接口</option></select></label>
            <label><span>资源路径</span><input v-model="form.resourcePath" placeholder="/api/system/**" /></label>
          </template>
          <label><span>状态</span><select v-model="form.status"><option value="enabled">启用</option><option value="disabled">停用</option></select></label>
          <button class="submit-action" type="submit" :disabled="loading"><Save :size="15" />保存</button>
        </form>

        <section v-if="tab === 'users' && selectedId" class="assignment-panel">
          <h3>分配角色</h3>
          <label v-for="role in roles" :key="role.id"><input v-model="assignedIds" type="checkbox" :value="role.id" /><span>{{ role.roleName }}</span><small>{{ dataScopeLabel(role.dataScope) }}</small></label>
          <button class="submit-action" type="button" @click="saveAssignments">保存角色授权</button>
        </section>

        <section v-if="tab === 'roles' && selectedId" class="assignment-panel">
          <h3>分配菜单与接口权限</h3>
          <label v-for="permission in permissions" :key="permission.id"><input v-model="assignedIds" type="checkbox" :value="permission.id" /><span>{{ permission.permissionName }}</span><small>{{ permission.permissionCode }}</small></label>
          <button class="submit-action" type="button" @click="saveAssignments">保存权限授权</button>
        </section>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Plus, RefreshCw, Save } from "lucide-vue-next";
import {
  assignRolePermissions, assignUserRoles, fetchRolePermissionIds, fetchSystemOrgs, fetchSystemPermissions,
  fetchSystemRoles, fetchSystemUsers, fetchUserRoleIds, saveSystemOrg, saveSystemPermission, saveSystemRole,
  saveSystemUser, type SystemOrg, type SystemPermission, type SystemRole, type SystemUser
} from "../services/api";

type TabKey = "orgs" | "users" | "roles" | "permissions";
type Item = SystemOrg | SystemUser | SystemRole | SystemPermission;

const tabs: { key: TabKey; label: string }[] = [{ key: "orgs", label: "组织架构" }, { key: "users", label: "用户管理" }, { key: "roles", label: "角色管理" }, { key: "permissions", label: "权限资源" }];
const tab = ref<TabKey>("users");
const orgs = ref<SystemOrg[]>([]);
const users = ref<SystemUser[]>([]);
const roles = ref<SystemRole[]>([]);
const permissions = ref<SystemPermission[]>([]);
const selectedId = ref("");
const assignedIds = ref<string[]>([]);
const form = ref<Record<string, any>>({});
const notice = ref("等待操作");
const loading = ref(false);

const currentTitle = computed(() => tabs.find((item) => item.key === tab.value)?.label ?? "权限管理");
const currentItems = computed<Item[]>(() => ({ orgs: orgs.value, users: users.value, roles: roles.value, permissions: permissions.value })[tab.value]);

onMounted(loadAll);

async function loadAll() {
  loading.value = true;
  try {
    [orgs.value, users.value, roles.value, permissions.value] = await Promise.all([fetchSystemOrgs(), fetchSystemUsers(), fetchSystemRoles(), fetchSystemPermissions()]);
    notice.value = "已刷新";
  } catch (error) {
    notice.value = error instanceof Error ? error.message : "加载失败";
  } finally { loading.value = false; }
}

function startCreate() {
  selectedId.value = "";
  assignedIds.value = [];
  form.value = { parentId: "0", orgType: "department", orgId: orgs.value[0]?.id ?? "", dataScope: "self", permissionType: "api", status: "enabled" };
}

async function selectItem(item: Item) {
  selectedId.value = String(item.id);
  form.value = { ...item, password: "" };
  assignedIds.value = tab.value === "users" ? await fetchUserRoleIds(selectedId.value) : tab.value === "roles" ? await fetchRolePermissionIds(selectedId.value) : [];
}

async function saveCurrent() {
  loading.value = true;
  try {
    if (tab.value === "orgs") await saveSystemOrg(form.value, selectedId.value || undefined);
    if (tab.value === "users") await saveSystemUser(form.value, selectedId.value || undefined);
    if (tab.value === "roles") await saveSystemRole(form.value, selectedId.value || undefined);
    if (tab.value === "permissions") await saveSystemPermission(form.value, selectedId.value || undefined);
    notice.value = "保存成功";
    await loadAll();
    startCreate();
  } catch (error) { notice.value = error instanceof Error ? error.message : "保存失败"; }
  finally { loading.value = false; }
}

async function saveAssignments() {
  if (!selectedId.value) return;
  if (tab.value === "users") await assignUserRoles(selectedId.value, assignedIds.value);
  if (tab.value === "roles") await assignRolePermissions(selectedId.value, assignedIds.value);
  notice.value = "授权已保存，用户重新登录后生效";
}

function itemName(item: Item) { return "realName" in item ? item.realName : "roleName" in item ? item.roleName : "permissionName" in item ? item.permissionName : item.orgName; }
function itemCode(item: Item) { return "username" in item ? item.username : "roleCode" in item ? item.roleCode : "permissionCode" in item ? item.permissionCode : item.orgCode; }
function statusLabel(value: string) { return value === "enabled" ? "启用" : "停用"; }
function dataScopeLabel(value: string) { return ({ all: "全部数据", org: "本部门", org_and_children: "本部门及下级", self: "仅本人" } as Record<string, string>)[value] ?? value; }
</script>
