(function () {
    function textOf(cell) {
        return (cell ? cell.textContent : "").replace(/\s+/g, " ").trim();
    }

    function normalize(value) {
        return (value || "").toLowerCase();
    }

    function fillAutoOptions(select, table) {
        var col = parseInt(select.getAttribute("data-filter-column"), 10);
        if (isNaN(col)) return;
        var values = [];
        table.querySelectorAll("tbody tr").forEach(function (row) {
            if (row.querySelector(".trong-rong")) return;
            var value = textOf(row.cells[col]);
            if (value && values.indexOf(value) === -1) values.push(value);
        });
        values.sort(function (a, b) { return a.localeCompare(b, "vi"); });
        values.forEach(function (value) {
            var option = document.createElement("option");
            option.value = value;
            option.textContent = value;
            select.appendChild(option);
        });
    }

    function applyFilter(panel, table) {
        var keywordInput = panel.querySelector("[data-filter-text]");
        var keyword = normalize(keywordInput ? keywordInput.value : "");
        var selects = Array.prototype.slice.call(panel.querySelectorAll("select[data-filter-column]"));
        var visible = 0;

        table.querySelectorAll("tbody tr").forEach(function (row) {
            if (row.querySelector(".trong-rong")) return;
            var rowText = normalize(row.textContent);
            var match = !keyword || rowText.indexOf(keyword) !== -1;

            selects.forEach(function (select) {
                if (!match || !select.value) return;
                var col = parseInt(select.getAttribute("data-filter-column"), 10);
                var cellText = textOf(row.cells[col]);
                if (cellText !== select.value) match = false;
            });

            row.dataset.filterHidden = match ? "false" : "true";
            if (match) visible++;
        });

        table.dispatchEvent(new CustomEvent('qlsv:table-filter'));

        var empty = panel.parentElement.querySelector("[data-filter-empty]");
        if (empty) empty.style.display = visible === 0 ? "" : "none";
    }

    function initPanel(panel) {
        var tableId = panel.getAttribute("data-filter-table");
        var table = tableId ? document.getElementById(tableId) : null;
        if (!table) return;

        panel.querySelectorAll("select[data-filter-auto]").forEach(function (select) {
            fillAutoOptions(select, table);
        });

        panel.querySelectorAll("input, select").forEach(function (control) {
            control.addEventListener("input", function () { applyFilter(panel, table); });
            control.addEventListener("change", function () { applyFilter(panel, table); });
        });

        var reset = panel.querySelector("[data-filter-reset]");
        if (reset) {
            reset.addEventListener("click", function () {
                panel.querySelectorAll("input").forEach(function (input) { input.value = ""; });
                panel.querySelectorAll("select").forEach(function (select) { select.value = ""; });
                applyFilter(panel, table);
            });
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".bo-loc-bang").forEach(initPanel);
    });
})();
