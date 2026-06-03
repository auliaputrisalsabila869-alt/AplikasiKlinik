public class Resep {
    private String idResep;
    private Obat obat;
    private int jumlah;
    private String aturanPakai;

    public Resep(String idResep, Obat obat, int jumlah, String aturanPakai) {
        this.idResep = idResep;
        this.obat = obat;
        this.jumlah = jumlah;
        this.aturanPakai = aturanPakai;
    }

    public String getIdResep() { return idResep; }
    public Obat getObat() { return obat; }
    public int getJumlah() { return jumlah; }
    public String getAturanPakai() { return aturanPakai; }

    public double getTotalHarga() {
        return obat.getHarga() * jumlah;
    }
}