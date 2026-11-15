(function () {
    var drawer = document.querySelector('[data-timetable-drawer]');
    if (!drawer) return;

    var previousFocus = null;
    var backgroundState = [];
    var focusableSelector = 'a[href], area[href], button:not([disabled]), '
        + 'input:not([disabled]):not([type="hidden"]), select:not([disabled]), '
        + 'textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

    function setDetail(selector, value) {
        var target = drawer.querySelector(selector);
        if (target) target.textContent = value || '';
    }

    function getFocusableElements() {
        return Array.prototype.slice.call(drawer.querySelectorAll(focusableSelector))
            .filter(function (element) {
                return element.getAttribute('aria-hidden') !== 'true';
            });
    }

    function focusFirstElement() {
        var focusable = getFocusableElements();
        (focusable[0] || drawer).focus();
    }

    function hideBackground() {
        var current = drawer;
        while (current.parentElement) {
            var parent = current.parentElement;
            Array.prototype.forEach.call(parent.children, function (sibling) {
                if (sibling === current) return;
                backgroundState.push({
                    element: sibling,
                    hadInert: sibling.hasAttribute('inert'),
                    hadAriaHidden: sibling.hasAttribute('aria-hidden'),
                    ariaHidden: sibling.getAttribute('aria-hidden')
                });
                sibling.setAttribute('inert', '');
                sibling.setAttribute('aria-hidden', 'true');
            });
            current = parent;
        }
    }

    function restoreBackground() {
        backgroundState.forEach(function (state) {
            if (state.hadInert) state.element.setAttribute('inert', '');
            else state.element.removeAttribute('inert');
            if (state.hadAriaHidden) {
                state.element.setAttribute('aria-hidden', state.ariaHidden);
            } else {
                state.element.removeAttribute('aria-hidden');
            }
        });
        backgroundState = [];
    }

    function trapFocus(event) {
        if (event.key !== 'Tab' || drawer.getAttribute('aria-hidden') === 'true') {
            return;
        }
        var focusable = getFocusableElements();
        if (!focusable.length) {
            event.preventDefault();
            drawer.focus();
            return;
        }
        var first = focusable[0];
        var last = focusable[focusable.length - 1];
        if (focusable.length === 1 || (event.shiftKey && document.activeElement === first)) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    }

    function open(eventButton) {
        previousFocus = eventButton;
        setDetail('[data-detail-code]', eventButton.dataset.code);
        setDetail('[data-detail-course]', eventButton.dataset.course);
        setDetail('[data-detail-date]', eventButton.dataset.date);
        setDetail('[data-detail-day]', eventButton.dataset.day);
        setDetail('[data-detail-shift]', eventButton.dataset.shift);
        setDetail('[data-detail-time]', eventButton.dataset.time);
        setDetail('[data-detail-room]', eventButton.dataset.room);
        setDetail('[data-detail-teacher]', eventButton.dataset.teacher);
        setDetail('[data-detail-category]', eventButton.dataset.category);
        setDetail('[data-detail-status]', eventButton.dataset.status);

        var edit = drawer.querySelector('[data-detail-edit]');
        if (edit) edit.href = eventButton.dataset.editUrl || '#';
        var id = drawer.querySelector('[data-detail-id]');
        if (id) id.value = eventButton.dataset.id || '';

        drawer.setAttribute('aria-hidden', 'false');
        drawer.classList.add('is-open');
        hideBackground();
        focusFirstElement();
    }

    function close() {
        if (drawer.getAttribute('aria-hidden') === 'true') return;
        drawer.setAttribute('aria-hidden', 'true');
        drawer.classList.remove('is-open');
        restoreBackground();
        if (previousFocus) previousFocus.focus();
    }

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-timetable-event]');
        if (button) {
            open(button);
            return;
        }
        if (event.target.closest('[data-timetable-close]')) close();
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') close();
        trapFocus(event);
    });

    document.addEventListener('focusin', function (event) {
        if (drawer.getAttribute('aria-hidden') === 'false' && !drawer.contains(event.target)) {
            focusFirstElement();
        }
    });
}());
