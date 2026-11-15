package com.example.practicetoeicapp.data;

import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;
import com.example.practicetoeicapp.hoctuvung.TuVung;
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;
import com.example.practicetoeicapp.sapxepcau.CauSapXep;
import com.example.practicetoeicapp.tracnghiem.CauTracNghiem;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionRepository {
    public interface DataCallback<T> {
        void onSuccess(T data);

        void onError(Exception e);
    }

    private static QuestionRepository instance;
    private final FirebaseFirestore db;

    private QuestionRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized QuestionRepository getInstance() {
        if (instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }

    public void loadBoHocTap(DataCallback<List<BoHocTap>> callback) {
        db.collection(FirestoreContract.COL_BO_CAU_HOI)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> callback.onSuccess(toBoHocTapList(snapshot)))
                .addOnFailureListener(callback::onError);
    }

    public void loadBoHocTapById(int idBo, DataCallback<BoHocTap> callback) {
        loadById(FirestoreContract.COL_BO_CAU_HOI, idBo,
                document -> toBoHocTap(document), callback);
    }

    public void createBoHocTap(String tenBo, DataCallback<Integer> callback) {
        loadBoHocTap(new DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                int maxStt = 0;
                for (BoHocTap item : data) {
                    if (item.getStt() > maxStt) {
                        maxStt = item.getStt();
                    }
                }
                final int stt = maxStt + 1;
                reserveId(FirestoreContract.COL_BO_CAU_HOI, new DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer id) {
                        Map<String, Object> values = new HashMap<>();
                        values.put(FirestoreContract.F_ID_BO, id);
                        values.put(FirestoreContract.F_STT, stt);
                        values.put(FirestoreContract.F_TEN_BO, tenBo);
                        db.collection(FirestoreContract.COL_BO_CAU_HOI)
                                .document(String.valueOf(id))
                                .set(values)
                                .addOnSuccessListener(unused -> callback.onSuccess(id))
                                .addOnFailureListener(callback::onError);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateBoHocTap(int idBo, int stt, String tenBo, DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_STT, stt);
        values.put(FirestoreContract.F_TEN_BO, tenBo);
        db.collection(FirestoreContract.COL_BO_CAU_HOI)
                .document(String.valueOf(idBo))
                .set(values, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void deleteBoHocTap(int idBo, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_BO_CAU_HOI, idBo, callback);
    }

    public void loadTracNghiem(int idBo, DataCallback<List<CauTracNghiem>> callback) {
        db.collection(FirestoreContract.COL_TRAC_NGHIEM)
                .whereEqualTo(FirestoreContract.F_ID_BO, idBo)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<CauTracNghiem> list = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        list.add(toTracNghiem(document));
                    }
                    Collections.sort(list, (a, b) -> Integer.compare(a.getIdcau(), b.getIdcau()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void loadTracNghiemById(int idCau, DataCallback<CauTracNghiem> callback) {
        loadById(FirestoreContract.COL_TRAC_NGHIEM, idCau, document -> toTracNghiem(document), callback);
    }

    public void createTracNghiem(int idBo, String noiDung, String dapAnA, String dapAnB,
                                 String dapAnC, String dapAnD, String dapAnTrue,
                                 DataCallback<Integer> callback) {
        reserveId(FirestoreContract.COL_TRAC_NGHIEM, new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                Map<String, Object> values = new HashMap<>();
                values.put(FirestoreContract.F_ID_CAU, id);
                values.put(FirestoreContract.F_ID_BO, idBo);
                values.put(FirestoreContract.F_NOI_DUNG, noiDung);
                values.put(FirestoreContract.F_DAP_AN_A, dapAnA);
                values.put(FirestoreContract.F_DAP_AN_B, dapAnB);
                values.put(FirestoreContract.F_DAP_AN_C, dapAnC);
                values.put(FirestoreContract.F_DAP_AN_D, dapAnD);
                values.put(FirestoreContract.F_DAP_AN_TRUE, dapAnTrue);
                createDocument(FirestoreContract.COL_TRAC_NGHIEM, id, values, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateTracNghiem(int idCau, int idBo, String noiDung, String dapAnA,
                                 String dapAnB, String dapAnC, String dapAnD,
                                 String dapAnTrue, DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_CAU, idCau);
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_NOI_DUNG, noiDung);
        values.put(FirestoreContract.F_DAP_AN_A, dapAnA);
        values.put(FirestoreContract.F_DAP_AN_B, dapAnB);
        values.put(FirestoreContract.F_DAP_AN_C, dapAnC);
        values.put(FirestoreContract.F_DAP_AN_D, dapAnD);
        values.put(FirestoreContract.F_DAP_AN_TRUE, dapAnTrue);
        updateDocument(FirestoreContract.COL_TRAC_NGHIEM, idCau, values, callback);
    }

    public void deleteTracNghiem(int idCau, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_TRAC_NGHIEM, idCau, callback);
    }

    public void loadDienKhuyet(int idBo, DataCallback<List<CauDienKhuyet>> callback) {
        db.collection(FirestoreContract.COL_DIEN_KHUYET)
                .whereEqualTo(FirestoreContract.F_ID_BO, idBo)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<CauDienKhuyet> list = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        list.add(toDienKhuyet(document));
                    }
                    Collections.sort(list, (a, b) -> Integer.compare(a.getIdcau(), b.getIdcau()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void loadDienKhuyetById(int idCau, DataCallback<CauDienKhuyet> callback) {
        loadById(FirestoreContract.COL_DIEN_KHUYET, idCau, document -> toDienKhuyet(document), callback);
    }

    public void createDienKhuyet(int idBo, String noiDung, String dapAn, String goiY,
                                 DataCallback<Integer> callback) {
        reserveId(FirestoreContract.COL_DIEN_KHUYET, new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                Map<String, Object> values = new HashMap<>();
                values.put(FirestoreContract.F_ID_CAU, id);
                values.put(FirestoreContract.F_ID_BO, idBo);
                values.put(FirestoreContract.F_NOI_DUNG, noiDung);
                values.put(FirestoreContract.F_DAP_AN, dapAn);
                values.put(FirestoreContract.F_GOI_Y, goiY);
                createDocument(FirestoreContract.COL_DIEN_KHUYET, id, values, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateDienKhuyet(int idCau, int idBo, String noiDung, String dapAn,
                                 String goiY, DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_CAU, idCau);
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_NOI_DUNG, noiDung);
        values.put(FirestoreContract.F_DAP_AN, dapAn);
        values.put(FirestoreContract.F_GOI_Y, goiY);
        updateDocument(FirestoreContract.COL_DIEN_KHUYET, idCau, values, callback);
    }

    public void deleteDienKhuyet(int idCau, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_DIEN_KHUYET, idCau, callback);
    }

    public void loadSapXepCau(int idBo, DataCallback<List<CauSapXep>> callback) {
        db.collection(FirestoreContract.COL_SAP_XEP_CAU)
                .whereEqualTo(FirestoreContract.F_ID_BO, idBo)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<CauSapXep> list = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        list.add(toSapXepCau(document));
                    }
                    Collections.sort(list, (a, b) -> Integer.compare(a.getIdcau(), b.getIdcau()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void loadSapXepCauById(int idCau, DataCallback<CauSapXep> callback) {
        loadById(FirestoreContract.COL_SAP_XEP_CAU, idCau, document -> toSapXepCau(document), callback);
    }

    public void createSapXepCau(int idBo, String dapAn, String part1, String part2,
                                String part3, String part4, DataCallback<Integer> callback) {
        reserveId(FirestoreContract.COL_SAP_XEP_CAU, new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                Map<String, Object> values = new HashMap<>();
                values.put(FirestoreContract.F_ID_CAU, id);
                values.put(FirestoreContract.F_ID_BO, idBo);
                values.put(FirestoreContract.F_DAP_AN, dapAn);
                values.put(FirestoreContract.F_PART_1, part1);
                values.put(FirestoreContract.F_PART_2, part2);
                values.put(FirestoreContract.F_PART_3, part3);
                values.put(FirestoreContract.F_PART_4, part4);
                createDocument(FirestoreContract.COL_SAP_XEP_CAU, id, values, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateSapXepCau(int idCau, int idBo, String dapAn, String part1,
                                String part2, String part3, String part4,
                                DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_CAU, idCau);
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_DAP_AN, dapAn);
        values.put(FirestoreContract.F_PART_1, part1);
        values.put(FirestoreContract.F_PART_2, part2);
        values.put(FirestoreContract.F_PART_3, part3);
        values.put(FirestoreContract.F_PART_4, part4);
        updateDocument(FirestoreContract.COL_SAP_XEP_CAU, idCau, values, callback);
    }

    public void deleteSapXepCau(int idCau, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_SAP_XEP_CAU, idCau, callback);
    }

    public void loadLuyenNghe(int idBo, DataCallback<List<CauLuyenNghe>> callback) {
        db.collection(FirestoreContract.COL_LUYEN_NGHE)
                .whereEqualTo(FirestoreContract.F_ID_BO, idBo)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<CauLuyenNghe> list = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        list.add(toLuyenNghe(document));
                    }
                    Collections.sort(list, (a, b) -> Integer.compare(a.getIdbai(), b.getIdbai()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void loadLuyenNgheById(int idBai, DataCallback<CauLuyenNghe> callback) {
        loadById(FirestoreContract.COL_LUYEN_NGHE, idBai, document -> toLuyenNghe(document), callback);
    }

    public void createLuyenNghe(int idBo, String dapAnA, String dapAnB, String dapAnC,
                                String dapAnD, String dapAnTrue, String hinhAnhUrl,
                                String audio, DataCallback<Integer> callback) {
        reserveId(FirestoreContract.COL_LUYEN_NGHE, new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                Map<String, Object> values = new HashMap<>();
                values.put(FirestoreContract.F_ID_BAI, id);
                values.put(FirestoreContract.F_ID_BO, idBo);
                values.put(FirestoreContract.F_DAP_AN_A, dapAnA);
                values.put(FirestoreContract.F_DAP_AN_B, dapAnB);
                values.put(FirestoreContract.F_DAP_AN_C, dapAnC);
                values.put(FirestoreContract.F_DAP_AN_D, dapAnD);
                values.put(FirestoreContract.F_DAP_AN_TRUE, dapAnTrue);
                values.put(FirestoreContract.F_HINH_ANH_URL, hinhAnhUrl);
                values.put(FirestoreContract.F_AUDIO, audio);
                createDocument(FirestoreContract.COL_LUYEN_NGHE, id, values, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateLuyenNghe(int idBai, int idBo, String dapAnA, String dapAnB,
                                String dapAnC, String dapAnD, String dapAnTrue,
                                String hinhAnhUrl, String audio, DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_BAI, idBai);
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_DAP_AN_A, dapAnA);
        values.put(FirestoreContract.F_DAP_AN_B, dapAnB);
        values.put(FirestoreContract.F_DAP_AN_C, dapAnC);
        values.put(FirestoreContract.F_DAP_AN_D, dapAnD);
        values.put(FirestoreContract.F_DAP_AN_TRUE, dapAnTrue);
        values.put(FirestoreContract.F_HINH_ANH_URL, hinhAnhUrl);
        values.put(FirestoreContract.F_AUDIO, audio);
        updateDocument(FirestoreContract.COL_LUYEN_NGHE, idBai, values, callback);
    }

    public void deleteLuyenNghe(int idBai, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_LUYEN_NGHE, idBai, callback);
    }

    public void loadTuVung(int idBo, DataCallback<List<TuVung>> callback) {
        db.collection(FirestoreContract.COL_TU_VUNG)
                .whereEqualTo(FirestoreContract.F_ID_BO, idBo)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<TuVung> list = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        list.add(toTuVung(document));
                    }
                    Collections.sort(list, (a, b) -> Integer.compare(a.getIdtu(), b.getIdtu()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void loadTuVungById(int idTu, DataCallback<TuVung> callback) {
        loadById(FirestoreContract.COL_TU_VUNG, idTu, document -> toTuVung(document), callback);
    }

    public void createTuVung(int idBo, String dapAn, String dichNghia, String loaiTu,
                             String audio, String anhUrl, DataCallback<Integer> callback) {
        reserveId(FirestoreContract.COL_TU_VUNG, new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                Map<String, Object> values = new HashMap<>();
                values.put(FirestoreContract.F_ID_TU, id);
                values.put(FirestoreContract.F_ID_BO, idBo);
                values.put(FirestoreContract.F_DAP_AN, dapAn);
                values.put(FirestoreContract.F_DICH_NGHIA, dichNghia);
                values.put(FirestoreContract.F_LOAI_TU, loaiTu);
                values.put(FirestoreContract.F_AUDIO, audio);
                values.put(FirestoreContract.F_ANH_URL, anhUrl);
                createDocument(FirestoreContract.COL_TU_VUNG, id, values, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateTuVung(int idTu, int idBo, String dapAn, String dichNghia,
                             String loaiTu, String audio, String anhUrl,
                             DataCallback<Void> callback) {
        Map<String, Object> values = new HashMap<>();
        values.put(FirestoreContract.F_ID_TU, idTu);
        values.put(FirestoreContract.F_ID_BO, idBo);
        values.put(FirestoreContract.F_DAP_AN, dapAn);
        values.put(FirestoreContract.F_DICH_NGHIA, dichNghia);
        values.put(FirestoreContract.F_LOAI_TU, loaiTu);
        values.put(FirestoreContract.F_AUDIO, audio);
        values.put(FirestoreContract.F_ANH_URL, anhUrl);
        updateDocument(FirestoreContract.COL_TU_VUNG, idTu, values, callback);
    }

    public void deleteTuVung(int idTu, DataCallback<Void> callback) {
        deleteDocument(FirestoreContract.COL_TU_VUNG, idTu, callback);
    }

    private void reserveId(String counterField, DataCallback<Integer> callback) {
        DocumentReference counter = db.collection(FirestoreContract.COL_META)
                .document(FirestoreContract.DOC_COUNTERS);
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counter);
            long current = 0;
            if (snapshot.exists()) {
                Long value = snapshot.getLong(counterField);
                if (value != null) {
                    current = value;
                }
            }
            long next = current + 1;
            Map<String, Object> values = new HashMap<>();
            values.put(counterField, next);
            transaction.set(counter, values, SetOptions.merge());
            return next;
        }).addOnSuccessListener(value -> callback.onSuccess(value.intValue()))
                .addOnFailureListener(callback::onError);
    }

    private void createDocument(String collection, int id, Map<String, Object> values,
                                DataCallback<Integer> callback) {
        db.collection(collection)
                .document(String.valueOf(id))
                .set(values)
                .addOnSuccessListener(unused -> callback.onSuccess(id))
                .addOnFailureListener(callback::onError);
    }

    private void updateDocument(String collection, int id, Map<String, Object> values,
                                DataCallback<Void> callback) {
        db.collection(collection)
                .document(String.valueOf(id))
                .set(values, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    private void deleteDocument(String collection, int id, DataCallback<Void> callback) {
        db.collection(collection)
                .document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    private interface DocumentMapper<T> {
        T map(DocumentSnapshot document);
    }

    private <T> void loadById(String collection, int id, DocumentMapper<T> mapper,
                              DataCallback<T> callback) {
        db.collection(collection)
                .document(String.valueOf(id))
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess(mapper.map(document));
                    } else {
                        callback.onError(new IllegalStateException("Document not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private List<BoHocTap> toBoHocTapList(QuerySnapshot snapshot) {
        List<BoHocTap> list = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            list.add(toBoHocTap(document));
        }
        Collections.sort(list, (a, b) -> Integer.compare(a.getStt(), b.getStt()));
        return list;
    }

    private BoHocTap toBoHocTap(DocumentSnapshot document) {
        return new BoHocTap(
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asInt(document.get(FirestoreContract.F_STT)),
                asString(document.get(FirestoreContract.F_TEN_BO)));
    }

    private CauTracNghiem toTracNghiem(DocumentSnapshot document) {
        return new CauTracNghiem(
                asInt(document.get(FirestoreContract.F_ID_CAU)),
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asString(document.get(FirestoreContract.F_NOI_DUNG)),
                asString(document.get(FirestoreContract.F_DAP_AN_A)),
                asString(document.get(FirestoreContract.F_DAP_AN_B)),
                asString(document.get(FirestoreContract.F_DAP_AN_C)),
                asString(document.get(FirestoreContract.F_DAP_AN_D)),
                asString(document.get(FirestoreContract.F_DAP_AN_TRUE)));
    }

    private CauDienKhuyet toDienKhuyet(DocumentSnapshot document) {
        return new CauDienKhuyet(
                asInt(document.get(FirestoreContract.F_ID_CAU)),
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asString(document.get(FirestoreContract.F_NOI_DUNG)),
                asString(document.get(FirestoreContract.F_DAP_AN)),
                asString(document.get(FirestoreContract.F_GOI_Y)));
    }

    private CauSapXep toSapXepCau(DocumentSnapshot document) {
        return new CauSapXep(
                asInt(document.get(FirestoreContract.F_ID_CAU)),
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asString(document.get(FirestoreContract.F_DAP_AN)),
                asString(document.get(FirestoreContract.F_PART_1)),
                asString(document.get(FirestoreContract.F_PART_2)),
                asString(document.get(FirestoreContract.F_PART_3)),
                asString(document.get(FirestoreContract.F_PART_4)));
    }

    private CauLuyenNghe toLuyenNghe(DocumentSnapshot document) {
        return new CauLuyenNghe(
                asInt(document.get(FirestoreContract.F_ID_BAI)),
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asString(document.get(FirestoreContract.F_DAP_AN_A)),
                asString(document.get(FirestoreContract.F_DAP_AN_B)),
                asString(document.get(FirestoreContract.F_DAP_AN_C)),
                asString(document.get(FirestoreContract.F_DAP_AN_D)),
                asString(document.get(FirestoreContract.F_DAP_AN_TRUE)),
                asString(document.get(FirestoreContract.F_HINH_ANH_URL)),
                asString(document.get(FirestoreContract.F_AUDIO)));
    }

    private TuVung toTuVung(DocumentSnapshot document) {
        return new TuVung(
                asInt(document.get(FirestoreContract.F_ID_TU)),
                asInt(document.get(FirestoreContract.F_ID_BO)),
                asString(document.get(FirestoreContract.F_DAP_AN)),
                asString(document.get(FirestoreContract.F_DICH_NGHIA)),
                asString(document.get(FirestoreContract.F_LOAI_TU)),
                asString(document.get(FirestoreContract.F_AUDIO)),
                asString(document.get(FirestoreContract.F_ANH_URL)));
    }

    private static int asInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
