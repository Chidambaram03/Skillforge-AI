// Central API Configuration
// Frontend (static HTML/CSS/JS) runs on port 3000,
// backend (Spring Boot API) runs on port 8080.
// Point all API calls explicitly to the backend.
const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    // Helper to get JWT token or userId from storage
    getUserId: () => localStorage.getItem('userId'),
    
    // Generic POST wrapper
    post: async (endpoint, data) => {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'API Request Failed');
            }
            
            return await response.json();
        } catch (error) {
            console.error('API POST Error:', error);
            throw error;
        }
    },

    // Generic GET wrapper
    get: async (endpoint) => {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'API Request Failed');
            }
            
            return await response.json();
        } catch (error) {
            console.error('API GET Error:', error);
            throw error;
        }
    }
};

// Global Logout Handler
document.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('userId');
            localStorage.removeItem('userName');
            window.location.href = 'login.html';
        });
    }

    // Quick Guard: Redirect to login if accessing protected page without ID
    const isProtected = ['dashboard.html', 'quiz.html', 'analytics.html'].some(page => window.location.pathname.includes(page));
    if (isProtected && !api.getUserId()) {
        window.location.href = 'login.html';
    }
});
