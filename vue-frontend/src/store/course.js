import { defineStore } from 'pinia'
import { ref } from 'vue'
import { courseApi } from '@/api/course.js'

export const useCourseStore = defineStore('course', () => {
  const courses = ref([])
  const selectedCourse = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const selectedCategory = ref('ALL')
  const categories = ['ALL', 'COMPLIANT', 'WATCH', 'RESTRICTED']

  async function fetchCourses() {
    loading.value = true
    error.value = null
    try {
      const response = await courseApi.getAll()
      courses.value = Array.isArray(response.data?.data) ? response.data.data : response.data || []
    } catch (cause) {
      error.value = cause.response?.data?.message || '종목 목록을 불러오지 못했습니다.'
      courses.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchCourse(id) {
    loading.value = true
    error.value = null
    try {
      const response = await courseApi.getById(id)
      selectedCourse.value = response.data?.data ?? response.data
    } catch (cause) {
      error.value = cause.response?.data?.message || '종목 정보를 불러오지 못했습니다.'
      selectedCourse.value = null
    } finally {
      loading.value = false
    }
  }

  function setCategory(value) { selectedCategory.value = value }

  return { courses, selectedCourse, loading, error, selectedCategory, categories, fetchCourses, fetchCourse, setCategory }
})
