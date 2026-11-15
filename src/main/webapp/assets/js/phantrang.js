/*
 * phan trang phia client cho moi bang danh sach.
 * tu dong ap dung cho moi <table> co so dong vuot qua nguong (mac dinh 10).
 * - bang it dong (ho so, bang key-value...) se khong bi anh huong vi duoi nguong.
 * - them data-no-paginate vao <table> de bo qua.
 * - chinh so dong moi trang bang data-page-size="20".
 * du lieu da nap san trong trang nen chi an/hien dong; cac o input van submit binh thuong.
 */
(function () {
    var MAC_DINH = 10;

    function taoNut(chu, khoa) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'pt-nut';
        b.textContent = chu;
        if (khoa) b.disabled = true;
        return b;
    }

    function danhSachTrang(cur, tong) {
        var arr = [];
        if (tong <= 7) {
            for (var i = 1; i <= tong; i++) arr.push(i);
            return arr;
        }
        arr.push(1);
        if (cur > 3) arr.push('...');
        var dau = Math.max(2, cur - 1);
        var cuoi = Math.min(tong - 1, cur + 1);
        for (var j = dau; j <= cuoi; j++) arr.push(j);
        if (cur < tong - 2) arr.push('...');
        arr.push(tong);
        return arr;
    }

    function phanTrang(bang) {
        var tbody = bang.tBodies[0];
        if (!tbody) return;
        var tatCaDong = Array.prototype.filter.call(tbody.children, function (r) {
            return r.tagName === 'TR';
        });
        var coTrang = parseInt(bang.getAttribute('data-page-size'), 10) || MAC_DINH;

        var trang = 1;
        var dieuKhien = null;
        var thongBaoRong = null;

        function ve() {
            var dongsHien = tatCaDong.filter(function (r) {
                return r.dataset.filterHidden !== 'true';
            });
            var soTrang = Math.ceil(dongsHien.length / coTrang);
            if (trang > soTrang && soTrang > 0) trang = soTrang;

            var dau = (trang - 1) * coTrang;
            var cuoi = dau + coTrang;

            tatCaDong.forEach(function (r) {
                if (r.dataset.filterHidden === 'true') {
                    r.style.display = 'none';
                } else {
                    var hienThi = dongsHien.indexOf(r);
                    r.style.display = (hienThi >= dau && hienThi < cuoi) ? '' : 'none';
                }
            });

            if (!dieuKhien) {
                dieuKhien = document.createElement('div');
                dieuKhien.className = 'pt-dieukhien';
                var moc = bang.closest('.bang-cuon') || bang;
                moc.parentNode.insertBefore(dieuKhien, moc.nextSibling);
            }
            if (!thongBaoRong) {
                thongBaoRong = document.createElement('div');
                thongBaoRong.className = 'trang-thai-rong';
                thongBaoRong.textContent = 'Không tìm thấy dữ liệu phù hợp';
                thongBaoRong.style.display = 'none';
                bang.parentNode.insertBefore(thongBaoRong, bang.nextSibling);
            }

            if (dongsHien.length === 0) {
                dieuKhien.style.display = 'none';
                thongBaoRong.style.display = '';
                bang.style.display = 'none';
                return;
            } else {
                bang.style.display = '';
                thongBaoRong.style.display = 'none';
                dieuKhien.style.display = '';
            }

            // Neu tong so dong <= co trang, an phan trang nhung van ve thong tin neu co filter
            if (tatCaDong.length <= coTrang && tatCaDong.length === dongsHien.length) {
                dieuKhien.style.display = 'none';
                return;
            }

            dieuKhien.innerHTML = '';
            var tt = document.createElement('span');
            tt.className = 'pt-thongtin';
            var tu = dongsHien.length > 0 ? dau + 1 : 0;
            var den = Math.min(trang * coTrang, dongsHien.length);
            tt.textContent = 'Hiển thị ' + tu + '–' + den + ' / ' + dongsHien.length;
            dieuKhien.appendChild(tt);

            if (soTrang > 1) {
                var nhom = document.createElement('div');
                nhom.className = 'pt-nhom';

                var truoc = taoNut('‹ Trước', trang === 1);
                truoc.onclick = function () { if (trang > 1) { trang--; ve(); } };
                nhom.appendChild(truoc);

                danhSachTrang(trang, soTrang).forEach(function (p) {
                    if (p === '...') {
                        var s = document.createElement('span');
                        s.className = 'pt-cham';
                        s.textContent = '…';
                        nhom.appendChild(s);
                        return;
                    }
                    var b = taoNut(String(p), false);
                    if (p === trang) b.classList.add('pt-active');
                    b.onclick = function () { trang = p; ve(); };
                    nhom.appendChild(b);
                });

                var sau = taoNut('Sau ›', trang === soTrang);
                sau.onclick = function () { if (trang < soTrang) { trang++; ve(); } };
                nhom.appendChild(sau);

                dieuKhien.appendChild(nhom);
            }
        }

        ve();

        bang.addEventListener('qlsv:table-filter', function () {
            trang = 1;
            ve();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        Array.prototype.forEach.call(document.querySelectorAll('table'), function (t) {
            if (t.hasAttribute('data-no-paginate')) return;
            phanTrang(t);
        });
    });
})();
