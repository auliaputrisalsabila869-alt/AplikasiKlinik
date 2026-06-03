public class Resep {
    private String idResep;
    private RekamMedis rekamMedis;
    private Obat obat;
    private int jumlah;
    private String aturanPakai;

    public Resep(String idResep, RekamMedis rekamMedis, Obat obat, int jumlah, String aturanPakai) {
        this.idResep = idResep;
        this.rekamMedis = rekamMedis;
        this.obat = obat;
        this.jumlah = jumlah;
        this.aturanPakai = aturanPakai;
    }

    public String getIdResep() { return idResep; }
    public RekamMedis getRekamMedis() { return rekamMedis; }
    public Obat getObat() { return obat; }
    public int getJumlah() { return jumlah; }
    public String getAturanPakai() { return aturanPakai; }

    public double getTotalHarga() {
        return obat.getHarga() * jumlah;
    }

    @Override
    public String toString() {
        return idResep + " - " + obat.getNamaObat();
    }
}