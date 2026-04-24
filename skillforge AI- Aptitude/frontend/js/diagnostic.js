document.addEventListener('DOMContentLoaded', () => {
    const userId = api.getUserId();
    if (!userId) {
        window.location.href = 'login.html';
        return;
    }

    let questions = [];
    let currentIndex = 0;
    let score = 0;
    let selectedOption = null;

    const introView = document.getElementById('introView');
    const quizView = document.getElementById('quizView');
    const resultView = document.getElementById('resultView');
    const startBtn = document.getElementById('startBtn');
    const questionText = document.getElementById('questionText');
    const optionsGrid = document.getElementById('optionsGrid');
    const progress = document.getElementById('progress');
    const nextBtn = document.getElementById('nextBtn');
    const levelResult = document.getElementById('levelResult');
    const finishBtn = document.getElementById('finishBtn');

    startBtn.addEventListener('click', async () => {
        try {
            questions = await api.get('/diagnostic/questions');
            introView.style.display = 'none';
            quizView.style.display = 'block';
            showQuestion();
        } catch (err) {
            alert('Failed to load questions. Make sure samples are loaded!');
        }
    });

    function showQuestion() {
        const q = questions[currentIndex];
        progress.textContent = `Question ${currentIndex + 1} of ${questions.length}`;
        questionText.textContent = q.questionText;
        optionsGrid.innerHTML = '';
        selectedOption = null;
        nextBtn.disabled = true;

        ['A', 'B', 'C', 'D'].forEach(opt => {
            const btn = document.createElement('button');
            btn.className = 'option-btn';
            btn.textContent = `${opt}: ${q['option' + opt]}`;
            btn.onclick = () => {
                document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                selectedOption = opt;
                nextBtn.disabled = false;
            };
            optionsGrid.appendChild(btn);
        });
    }

    nextBtn.addEventListener('click', () => {
        if (selectedOption === questions[currentIndex].correctOption) {
            score++;
        }

        currentIndex++;
        if (currentIndex < questions.length) {
            showQuestion();
        } else {
            completeDiagnostic();
        }
    });

    async function completeDiagnostic() {
        quizView.style.display = 'none';
        resultView.style.display = 'block';

        try {
            const res = await api.post(`/diagnostic/submit/${userId}`, { score: score });
            levelResult.textContent = `Based on your performance, we've placed you at Level ${res.level}!`;
        } catch (err) {
            levelResult.textContent = "We encountered an error saving your results, but you can continue to the dashboard.";
        }
    }

    finishBtn.addEventListener('click', () => {
        window.location.href = 'dashboard.html';
    });
});
