document.addEventListener('DOMContentLoaded', async () => {
    const userId = api.getUserId();
    if (!userId) return;

    // UI Elements
    const nameDisplay = document.getElementById('userNameDisplay');
    const levelDisplay = document.getElementById('thinkingLevelDisplay');
    const accuracyDisplay = document.getElementById('accuracyDisplay');
    const speedDisplay = document.getElementById('speedDisplay');

    // Display Name from storage
    nameDisplay.textContent = localStorage.getItem('userName') || 'Student';

    try {
        // Fetch Profile from Backend (Now returns {profile, topicMastery})
        const data = await api.get(`/quiz/profile/${userId}`);
        const profile = data.profile;
        const topicMastery = data.topicMastery;
        
        // Update UI Cards
        levelDisplay.textContent = profile.thinkingLevel || 'L1';
        
        const acc = profile.accuracyPercentage || 0;
        accuracyDisplay.textContent = `${acc.toFixed(1)}%`;
        
        const speedMs = profile.avgResponseTimeMs || 0;
        speedDisplay.textContent = speedMs === 0 ? '--s' : `${(speedMs / 1000).toFixed(1)}s`;

        // Render Heatmap Chart with actual data
        renderHeatmap(topicMastery);
    } catch (err) {
        console.error('Failed to load dashboard data:', err);
    }
});

function renderHeatmap(masteryData) {
    const ctx = document.getElementById('heatmapChart');
    if (!ctx || !masteryData) return;

    const labels = Object.keys(masteryData);
    const scores = Object.values(masteryData);

    // If no data yet, show empty state or a friendly message
    if (labels.length === 0) {
        ctx.style.display = 'none';
        const parent = ctx.parentElement;
        const msg = document.createElement('p');
        msg.innerText = "Start your first quiz to see your cognitive map!";
        msg.style.padding = "40px";
        msg.style.textAlign = "center";
        msg.style.color = "#888";
        parent.appendChild(msg);
        return;
    }

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Mastery Score (%)',
                data: scores,
                backgroundColor: function(context) {
                    const value = context.dataset.data[context.dataIndex];
                    if (value >= 80) return 'rgba(40, 167, 69, 0.7)'; // Green (Strong)
                    if (value >= 60) return 'rgba(255, 193, 7, 0.7)'; // Yellow (Moderate)
                    return 'rgba(220, 53, 69, 0.7)'; // Red (Weak)
                },
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}
