// Landing Page JavaScript - js/landing.js

// DOM Content Loaded Event
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    initializeScrollEffects();
});

// Initialize all event listeners
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

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Form validation on input
    setupFormValidation();
}

// Initialize scroll effects
function initializeScrollEffects() {
    // Navbar background on scroll
    window.addEventListener('scroll', function() {
        const navbar = document.querySelector('.navbar');
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
    });

    // Animate stats on scroll
    observeStats();
    
    // Animate feature cards on scroll
    observeFeatures();
}

// Modal Functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Focus on first input
        setTimeout(() => {
            const firstInput = modal.querySelector('input');
            if (firstInput) firstInput.focus();
        }, 100);
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Clear form
        const form = modal.querySelector('form');
        if (form) {
            form.reset();
            clearFormErrors(form);
        }
    }
}

function switchModal(currentModalId, newModalId) {
    closeModal(currentModalId);
    setTimeout(() => {
        openModal(newModalId);
    }, 200);
}

// Form Validation Functions
function validateLoginForm(form) {
    let isValid = true;
    
    const email = form.querySelector('#loginEmail');
    const password = form.querySelector('#loginPassword');
    
    if (!email.value.trim()) {
        showFieldError(email, 'Username is required');
        isValid = false;
    }
    
    if (!password.value.trim()) {
        showFieldError(password, 'Password is required');
        isValid = false;
    } else if (password.value.length < 6) {
        showFieldError(password, 'Password must be at least 6 characters');
        isValid = false;
    }
    
    return isValid;
}

function validateAdminForm(form) {
    let isValid = true;
    
    const username = form.querySelector('#adminEmail');
    const password = form.querySelector('#adminPassword');
    
    if (!username.value.trim()) {
        showFieldError(username, 'Admin username is required');
        isValid = false;
    }
    
    if (!password.value.trim()) {
        showFieldError(password, 'Password is required');
        isValid = false;
    }
    
    return isValid;
}

// Form Validation Setup
function setupFormValidation() {
    document.querySelectorAll('input').forEach(input => {
        input.addEventListener('blur', function() {
            validateField(this);
        });
        
        input.addEventListener('input', function() {
            clearFieldError(this);
        });
    });
}

function validateField(field) {
    const fieldName = field.name;
    const value = field.value.trim();
    
    switch(fieldName) {
        case 'password':
        case 'adminPassword':
            if (value && value.length < 6) {
                showFieldError(field, 'Password must be at least 6 characters');
            }
            break;
    }
}

// Utility Functions
function showFieldError(field, message) {
    clearFieldError(field);
    
    field.style.borderColor = '#ef4444';
    field.style.background = '#fef2f2';
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.style.color = '#ef4444';
    errorDiv.style.fontSize = '0.875rem';
    errorDiv.style.marginTop = '0.25rem';
    errorDiv.textContent = message;
    
    field.parentNode.appendChild(errorDiv);
}

function clearFieldError(field) {
    field.style.borderColor = '#e5e7eb';
    field.style.background = '#f9fafb';
    
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }
}

function clearFormErrors(form) {
    form.querySelectorAll('.field-error').forEach(error => error.remove());
    form.querySelectorAll('input').forEach(input => {
        input.style.borderColor = '#e5e7eb';
        input.style.background = '#f9fafb';
    });
    
    const existingMessage = form.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }
}

// Animation Functions
function observeStats() {
    const stats = document.querySelectorAll('.stat-number');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateNumber(entry.target);
                observer.unobserve(entry.target);
            }
        });
    });
    
    stats.forEach(stat => observer.observe(stat));
}

function animateNumber(element) {
    const target = parseInt(element.textContent.replace(/[^0-9]/g, ''));
    const duration = 2000;
    const startTime = performance.now();
    const suffix = element.textContent.replace(/[0-9,]/g, '');
    
    function updateNumber(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const current = Math.floor(target * progress);
        
        element.textContent = current.toLocaleString() + suffix;
        
        if (progress < 1) {
            requestAnimationFrame(updateNumber);
        }
    }
    
    requestAnimationFrame(updateNumber);
}

function observeFeatures() {
    const features = document.querySelectorAll('.feature-card');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });
    
    features.forEach((feature, index) => {
        feature.style.opacity = '0';
        feature.style.transform = 'translateY(50px)';
        feature.style.transition = `all 0.6s ease ${index * 0.1}s`;
        observer.observe(feature);
    });
}

// Mobile menu handling
function toggleMobileMenu() {
    const navMenu = document.querySelector('.nav-menu');
    const hamburger = document.querySelector('.hamburger');
    
    navMenu.classList.toggle('active');
    hamburger.classList.toggle('active');
}

// Close mobile menu when clicking on links
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
        const navMenu = document.querySelector('.nav-menu');
        const hamburger = document.querySelector('.hamburger');
        
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

console.log('LibraryHub landing page loaded successfully!');
