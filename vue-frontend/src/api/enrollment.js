import api from './index.js'

export const enrollmentApi = {
  getMySubscriptions() {
    return api.get('/api/enrollments/subscriptions/my')
  },
  subscribe(payload) {
    return api.post('/api/enrollments/subscriptions', payload)
  },
  changeSubscriptionStatus(id, status) {
    return api.patch(`/api/enrollments/subscriptions/${id}/status`, { status })
  },
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
  },
  enroll(payload) {
    return api.post('/api/enrollments', payload)
  },
  getAlternatives(ticker, clientId) {
    return api.get(`/api/recommend/alternatives/${ticker}`, { params: { clientId } })
  },
  getRecommendations(clientId) {
    return api.get(`/api/recommend/${clientId}`)
  },
  getAnomalies() {
    return api.get('/api/recommend/anomalies')
  }
}
