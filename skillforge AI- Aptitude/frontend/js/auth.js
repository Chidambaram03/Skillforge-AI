document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const errorMessage = document.getElementById('errorMessage');

    const showError = (msg) => {
        errorMessage.textContent = msg;
        errorMessage.classList.remove('hidden');
    };

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const button = loginForm.querySelector('button');
                button.textContent = 'Logging In...';
                button.disabled = true;

                const response = await api.post('/auth/login', { email, password });
                
                // Save session
                localStorage.setItem('userId', response.userId);
                localStorage.setItem('userName', response.userName);
                
                // Redirect
                window.location.href = 'dashboard.html';
            } catch (err) {
                showError(err.message || 'Invalid email or password');
                const button = loginForm.querySelector('button');
                button.textContent = 'Log In';
                button.disabled = false;
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const button = registerForm.querySelector('button');
                button.textContent = 'Signing Up...';
                button.disabled = true;

                const response = await api.post('/auth/register', { name, email, password });
                
                // Save session & Redirect to Diagnostic
                localStorage.setItem('userId', response.userId);
                localStorage.setItem('userName', name);
                window.location.href = 'diagnostic.html';
            } catch (err) {
                showError(err.message || 'Registration failed');
                const button = registerForm.querySelector('button');
                button.textContent = 'Sign Up';
                button.disabled = false;
            }
        });
    }
});
