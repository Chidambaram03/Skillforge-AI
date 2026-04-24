document.addEventListener('DOMContentLoaded', async () => {
    const userId = api.getUserId();
    if (!userId) return;

    try {
        const data = await api.get(`/quiz/profile/${userId}`);
        const profile = data.profile;
        const topicMastery = data.topicMastery;

        renderAccuracyChart(profile.accuracyPercentage || 0);
        renderSpeedChart(profile.avgResponseTimeMs || 0);
        renderTopicMasteryChart(topicMastery);
    } catch (err) {
        console.error('Failed to load analytics data:', err);
    }
});

function renderAccuracyChart(currentAcc) {
    const ctx = document.getElementById('accuracyChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Baseline', 'Session 1', 'Session 2', 'Current'],
            datasets: [{
                label: 'Accuracy %',
                data: [0, 20, 50, currentAcc],
                borderColor: '#0056b3',
                backgroundColor: 'rgba(0, 86, 179, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { min: 0, max: 100 } }
        }
    });
}

function renderSpeedChart(currentSpeedMs) {
    const ctx = document.getElementById('speedChart');
    if (!ctx) return;

    const currentSec = (currentSpeedMs / 1000).toFixed(1);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Baseline', 'Session 1', 'Session 2', 'Current'],
            datasets: [{
                label: 'Avg Response (sec)',
                data: [60, 45, 30, currentSec], 
                borderColor: '#28a745',
                backgroundColor: 'rgba(40, 167, 69, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { suggestedMin: 0 } }
        }
    });
}

function renderTopicMasteryChart(masteryData) {
    const ctx = document.getElementById('topicMasteryChart');
    if (!ctx || !masteryData) return;

    const labels = Object.keys(masteryData);
    const scores = Object.values(masteryData);

    if (labels.length === 0) return;

    new Chart(ctx, {
        type: 'radar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Your Mastery',
                data: scores,
                backgroundColor: 'rgba(0, 86, 179, 0.2)',
                borderColor: '#0056b3',
                pointBackgroundColor: '#0056b3'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: { suggestedMin: 0, suggestedMax: 100 }
            }
        }
    });
}
