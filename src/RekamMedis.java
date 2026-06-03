import java.util.ArrayList;

public class RekamMedis {
    private String idRekamMedis;
    private Pasien pasien;
    private Dokter dokter;
    private String tanggal;
    private String keluhan;
    private String diagnosis;
    private ArrayList<Resep> daftarResep;

    public RekamMedis(String idRekamMedis, Pasien pasien, Dokter dokter,
                      String tanggal, String keluhan, String diagnosis) {
        this.idRekamMedis = idRekamMedis;
        this.pasien = pasien;
        this.dokter = dokter;
        this.tanggal = tanggal;
        this.keluhan = keluhan;
        this.diagnosis = diagnosis;
        this.daftarResep = new ArrayList<>();
    }

    public void tambahResep(Resep resep) { daftarResep.add(resep); }

    public String getIdRekamMedis() { return idRekamMedis; }
    public Pasien getPasien() { return pasien; }
    public Dokter getDokter() { return dokter; }
    public Dokter getDoctor() { return dokter; }
    public String getTanggal() { return tanggal; }
    public String getKeluhan() { return keluhan; }
    public String getDiagnosis() { return diagnosis; }
    public ArrayList<Resep> getDaftarResep() { return daftarResep; }

    public double getTotalBiayaObat() {
        double total = 0;
        for (Resep r : daftarResep) total += r.getTotalHarga();
        return total;
    }

    @Override
    public String toString() {
        return idRekamMedis + " - " + (pasien != null ? pasien.getNama() : "?");
    }
}