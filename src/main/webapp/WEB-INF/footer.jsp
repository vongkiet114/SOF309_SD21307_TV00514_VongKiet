<%-- phan cuoi dung chung: dong the + script dieu khien sidebar tren dien thoai --%>
    </main>
</div>
<script>
    (function ganCsrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        if (!meta) return;
        document.querySelectorAll('form').forEach(function (form) {
            var method = (form.getAttribute('method') || 'get').toLowerCase();
            if (method !== 'post' || form.querySelector('input[name="_csrf"]')) return;
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = '_csrf';
            input.value = meta.content;
            form.appendChild(input);
        });
    })();

    function moSidebar() {
        document.getElementById("sidebar").classList.add("mo");
        document.getElementById("lopPhu").classList.add("hien");
    }
    function dongSidebar() {
        document.getElementById("sidebar").classList.remove("mo");
        document.getElementById("lopPhu").classList.remove("hien");
    }

    function doiNhomMenu(nut) {
        var nhom = nut.closest(".menu-nhom");
        if (nhom) nhom.classList.toggle("mo");
    }
</script>
<script src="<%= request.getContextPath() %>/assets/js/phantrang.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/bolocbang.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/timetable.js"></script>
</body>
</html>
