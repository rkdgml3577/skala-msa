<script setup>
import { ref, onMounted } from 'vue'
import { userApi, itemApi } from './api'

const users = ref([])
const items = ref([])
const error = ref('')

const newUser = ref({ username: '', email: '', displayName: '' })
const newItem = ref({ code: '', name: '', price: 0 })

function unwrap(res) {
  // 백엔드 공통 응답 래퍼 { success, message, data } 처리
  const body = res.data
  return body && body.data !== undefined ? body.data : body
}

async function loadUsers() {
  try {
    users.value = unwrap(await userApi.list())
  } catch (e) {
    error.value = 'users 조회 실패: ' + e.message
  }
}

async function loadItems() {
  try {
    items.value = unwrap(await itemApi.list())
  } catch (e) {
    error.value = 'items 조회 실패: ' + e.message
  }
}

async function createUser() {
  try {
    await userApi.create(newUser.value)
    newUser.value = { username: '', email: '', displayName: '' }
    await loadUsers()
  } catch (e) {
    error.value = 'user 생성 실패: ' + e.message
  }
}

async function createItem() {
  try {
    await itemApi.create({ ...newItem.value, price: Number(newItem.value.price) })
    newItem.value = { code: '', name: '', price: 0 }
    await loadItems()
  } catch (e) {
    error.value = 'item 생성 실패: ' + e.message
  }
}

onMounted(() => {
  loadUsers()
  loadItems()
})
</script>

<template>
  <main class="wrap">
    <h1>MSA Base Template</h1>
    <p class="hint">user-service / course-service 를 Gateway(/api) 로 호출하는 최소 예시 화면입니다.</p>
    <p v-if="error" class="error">{{ error }}</p>

    <section class="grid">
      <div class="card">
        <h2>Users</h2>
        <form @submit.prevent="createUser">
          <input v-model="newUser.username" placeholder="username" required />
          <input v-model="newUser.email" placeholder="email" required />
          <input v-model="newUser.displayName" placeholder="displayName" required />
          <button type="submit">생성</button>
        </form>
        <ul>
          <li v-for="u in users" :key="u.id">#{{ u.id }} {{ u.username }} ({{ u.displayName }})</li>
        </ul>
      </div>

      <div class="card">
        <h2>Items</h2>
        <form @submit.prevent="createItem">
          <input v-model="newItem.code" placeholder="code" required />
          <input v-model="newItem.name" placeholder="name" required />
          <input v-model="newItem.price" type="number" placeholder="price" required />
          <button type="submit">생성</button>
        </form>
        <ul>
          <li v-for="i in items" :key="i.id">#{{ i.id }} {{ i.name }} — {{ i.price }}</li>
        </ul>
      </div>
    </section>
  </main>
</template>

<style>
body { margin: 0; font-family: system-ui, sans-serif; background: #f5f6f8; color: #1c1c1e; }
.wrap { max-width: 900px; margin: 0 auto; padding: 32px 20px; }
h1 { margin: 0 0 4px; }
.hint { color: #666; margin-top: 0; }
.error { color: #c0392b; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.card { background: #fff; border: 1px solid #e3e5e8; border-radius: 10px; padding: 16px; }
form { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
input { padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
button { padding: 8px; border: none; border-radius: 6px; background: #2d6cdf; color: #fff; cursor: pointer; }
button:hover { background: #1e57bd; }
ul { padding-left: 18px; margin: 0; }
li { padding: 2px 0; }
@media (max-width: 640px) { .grid { grid-template-columns: 1fr; } }
</style>
