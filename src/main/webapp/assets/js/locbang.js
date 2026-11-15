(function () {
    function chuanHoa(value) {
        return (value || '').toLocaleLowerCase('vi')
            .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd');
    }
    document.addEventListener('input', function (event) {
        var id = event.target.getAttribute('data-table-filter');
        if (!id) return;
        var table = document.getElementById(id);
        if (!table || !table.tBodies[0]) return;
        var query = chuanHoa(event.target.value.trim());
        Array.prototype.forEach.call(table.tBodies[0].rows, function (row) {
            row.dataset.filterHidden = chuanHoa(row.textContent).indexOf(query) < 0 ? 'true' : 'false';
        });
        table.dispatchEvent(new CustomEvent('qlsv:table-filter'));
    });
})();
