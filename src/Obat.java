public class Obat {
    private String idObat;
    private String namaObat;
    private String jenis;
    private int stok;
    private double harga;

    public Obat(String idObat, String namaObat, String jenis, int stok, double harga) {
        this.idObat = idObat;
        this.namaObat = namaObat;
        this.jenis = jenis;
        this.stok = stok;
        this.harga = harga;
    }

    public String getIdObat() { return idObat; }
    public String getNamaObat() { return namaObat; }
    public String getJenis() { return jenis; }
    public int getStok() { return stok; }
    public double getHarga() { return harga; }
    
    @Override
    public String toString() {
        return namaObat;
    }

    public void kurangiStok(int jumlah) {
        if (stok >= jumlah) stok -= jumlah;
    }

    public String[] toTableRow() {
        return new String[]{idObat, namaObat, jenis, String.valueOf(stok), String.valueOf(harga)};
    }
}