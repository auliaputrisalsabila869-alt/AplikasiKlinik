public class Pembayaran {
    private String idPembayaran;
    private RekamMedis rekamMedis;
    private double biayaKonsultasi;
    private double biayaObat;
    private double totalBayar;
    private String tanggal;
    private String status; // "lunas" atau "belum"

    public Pembayaran(String idPembayaran, RekamMedis rekamMedis,
                      double biayaKonsultasi, String tanggal) {
        this.idPembayaran = idPembayaran;
        this.rekamMedis = rekamMedis;
        this.biayaKonsultasi = biayaKonsultasi;
        this.biayaObat = rekamMedis.getTotalBiayaObat();
        this.totalBayar = biayaKonsultasi + biayaObat;
        this.tanggal = tanggal;
        this.status = "belum";
    }

    public void bayar() { this.status = "lunas"; }

    public String getIdPembayaran() { return idPembayaran; }
    public double getBiayaKonsultasi() { return biayaKonsultasi; }
    public double getTotalBayar() { return totalBayar; }
    public String getStatus() { return status; }
    public RekamMedis getRekamMedis() { return rekamMedis; }

    public String[] toTableRow() {
        return new String[]{
            idPembayaran, tanggal,
            rekamMedis.getPasien().getNama(),
            String.valueOf(biayaKonsultasi),
            String.valueOf(totalBayar), status
        };
    }
    public void setStatus(String status) { this.status = status; }
}