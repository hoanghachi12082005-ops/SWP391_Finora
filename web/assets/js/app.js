document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.module-card').forEach((card, index) => {
    card.style.animation = `riseIn .45s ease ${Math.min(index * 18, 420)}ms both`;
  });
});
const style = document.createElement('style');
style.textContent = '@keyframes riseIn { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: translateY(0); } }';
document.head.appendChild(style);
