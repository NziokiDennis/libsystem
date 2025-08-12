// Landing Page JavaScript - js/landing.js

// DOM Content Loaded Event
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
});

// Initialize event listeners
function initializeEventListeners() {
    // Hamburger menu toggle
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    if (hamburger) {
        hamburger.addEventListener('click', function() {
            navMenu.classList.toggle('active');
            hamburger.classList.toggle('active');
        });
    }

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            closeModal(event.target.id);
        }
    });

    // Close mobile menu when clicking links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            if (navMenu.classList.contains('active')) {
                navMenu.classList.remove('active');
                hamburger.classList.remove('active');
            }
        });
    });

    // Keyboard accessibility for modals
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            const openModal = document.querySelector('.modal[style*="display: block"]');
            if (openModal) {
                closeModal(openModal.id);
            }
        }
    });
}

// Modal Functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        const firstInput = modal.querySelector('input');
        if (firstInput) firstInput.focus();
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        const form = modal.querySelector('form');
        if (form) form.reset();
    }
}

function switchModal(currentModalId, newModalId) {
    closeModal(currentModalId);
    setTimeout(() => openModal(newModalId), 200);
}

console.log('Libsystems landing page loaded successfully!');