import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Antrean {
    private Map<String, Queue<Pasien>> mapQueue = new HashMap<>();
    private Map<String, Integer> mapCounter = new HashMap<>();
    private Map<Pasien, String> mapKodeAntrean = new HashMap<>();
    private Map<String, String> mapSedangDitangani = new HashMap<>();
    private Map<String, Pasien> mapPasienDitangani = new HashMap<>();

    public String tambahAntrean(String idDokter, Pasien p) {
        mapQueue.putIfAbsent(idDokter, new LinkedList<>());
        mapCounter.putIfAbsent(idDokter, 0);
        int counterTerbaru = mapCounter.get(idDokter) + 1;
        mapCounter.put(idDokter, counterTerbaru);
        String kodeAntrean = String.format("A-%03d", counterTerbaru);
        mapQueue.get(idDokter).add(p);
        mapKodeAntrean.put(p, kodeAntrean);
        return kodeAntrean;
    }

    public Pasien panggilBerikutnya(String idDokter) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q != null && !q.isEmpty()) {
            Pasien p = q.poll();
            String kode = mapKodeAntrean.get(p);
            mapSedangDitangani.put(idDokter, kode);
            mapPasienDitangani.put(idDokter, p);
            return p;
        }
        return null;
    }

    public int getJumlahAntrean(String idDokter) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        return q != null ? q.size() : 0;
    }

    public int getTotalSemuaAntrean() {
        int total = 0;
        for (Queue<Pasien> q : mapQueue.values()) total += q.size();
        return total;
    }

    public ArrayList<String[]> getDaftarAntreanString(String idDokter) {
        ArrayList<String[]> daftar = new ArrayList<>();
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q != null) {
            for (Pasien p : q) {
                String kode = mapKodeAntrean.getOrDefault(p, "A-000");
                daftar.add(new String[]{kode, p.getIdPasien(), p.getNama()});
            }
        }
        return daftar;
    }

    public String getKodeAntrean(Pasien p) {
        return mapKodeAntrean.getOrDefault(p, "-");
    }

    public String getSedangDitangani(String idDokter) {
        return mapSedangDitangani.getOrDefault(idDokter, "Belum ada");
    }

    public Pasien getPasienSedangDitangani(String idDokter) {
        return mapPasienDitangani.get(idDokter);
    }

    public int getSisaDidepan(String idDokter, Pasien p) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q == null) return -1;
        int count = 0;
        for (Pasien pasienDiDalamQueue : q) {
            if (pasienDiDalamQueue.getIdPasien().equals(p.getIdPasien())) return count;
            count++;
        }
        return -1;
    }

    public void inisialisasiCounter(String idDokter, int nilai) {
        mapCounter.put(idDokter, nilai);
        mapQueue.putIfAbsent(idDokter, new LinkedList<>());
    }

    public Queue<Pasien> getAntrean(String idDokter) {
        mapQueue.putIfAbsent(idDokter, new LinkedList<>());
        return this.mapQueue.get(idDokter);
    }
}