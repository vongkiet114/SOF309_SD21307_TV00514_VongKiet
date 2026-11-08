package Models;

public class MonHocTienQuyet {

    private int maMonhoc;
    private int maMonhocTienquyet;

    public MonHocTienQuyet() {
    }

    public MonHocTienQuyet(int maMonhoc, int maMonhocTienquyet) {
        this.maMonhoc = maMonhoc;
        this.maMonhocTienquyet = maMonhocTienquyet;
    }

    public int getMaMonhoc() {
        return maMonhoc;
    }

    public void setMaMonhoc(int maMonhoc) {
        this.maMonhoc = maMonhoc;
    }

    public int getMaMonhocTienquyet() {
        return maMonhocTienquyet;
    }

    public void setMaMonhocTienquyet(int maMonhocTienquyet) {
        this.maMonhocTienquyet = maMonhocTienquyet;
    }

    @Override
    public String toString() {
        return "MonHocTienQuyet{" +
                "maMonhoc=" + maMonhoc +
                ", maMonhocTienquyet=" + maMonhocTienquyet +
                '}';
    }
}
