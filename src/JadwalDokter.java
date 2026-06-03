public class JadwalDokter {
    private String idJadwal;
    private Dokter dokter;
    private String hari;
    private String jamMulai;
    private String jamSelesai;
    private int kuota;

    public JadwalDokter(String idJadwal, Dokter dokter, String hari,
                        String jamMulai, String jamSelesai, int kuota) {
        this.idJadwal = idJadwal;
        this.dokter = dokter;
        this.hari = hari;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.kuota = kuota;
    }

    public String getIdJadwal() { return idJadwal; }
    public Dokter getDokter() { return dokter; }
    public Dokter getDoctor() { return dokter; }
    public String getHari() { return hari; }
    public String getJamMulai() { return jamMulai; }
    public String getJamSelesai() { return jamSelesai; }
    public int getKuota() { return kuota; }
}