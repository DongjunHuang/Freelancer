import http from './http'

// Upload CSV file
export const uploadCsv = (formData: FormData) => http.post('/upload/uploadCSV', formData); 