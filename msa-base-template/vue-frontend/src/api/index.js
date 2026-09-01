import axios from 'axios'

// 모든 요청은 /api 로 나가고, vite dev 프록시(또는 nginx)가 Gateway 로 전달한다.
const api = axios.create({ baseURL: '/api' })

export const userApi = {
  list: () => api.get('/users'),
  create: (payload) => api.post('/users', payload)
}

export const itemApi = {
  list: () => api.get('/courses'),
  create: (payload) => api.post('/courses', payload)
}

export default api
