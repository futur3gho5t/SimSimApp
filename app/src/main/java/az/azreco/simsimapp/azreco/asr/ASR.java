package az.azreco.simsimapp.azreco.asr;


import android.media.AudioRecord;
import android.util.Log;

import com.AndroidMic;
import com.Callbacks;
import com.EnergyVad;
import com.azreco.asr.client.ASRClient;
import com.azreco.asr.client.ASRClientConfiguration;
import com.azreco.asr.client.AudioSource;
import com.azreco.asr.client.ResultType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.List;

import static az.azreco.simsimapp.constant.AzrecoConstants.ASR_HOST;
import static az.azreco.simsimapp.constant.AzrecoConstants.ASR_PORT;
import static az.azreco.simsimapp.constant.AzrecoConstants.AZ_LANG;

public class ASR implements Callbacks {
    private static final int FRAME_MS = 100;
    private static final int FRAME_RATE = 16000;
    private static final int HEAD_MARGIN = 600;
    private static final int TAILMARGIN = 1500;
    private static final String TAG = "SpeechToText";

    private Thread recorderThread;/* Thread for running the Loop */
    private Thread kwsThread;/* Thread for running the Loop */
    private Thread asrThread;/* Thread for running the Loop */
    private int frameSizeInBytes;
    private int frameSizeInSamples;
    private AudioRecord recorder = null;
    private ASRClient asrClient = null;
    private ASRClient kwsClient = null;
    private AndroidMic androidMic = null;
    private EnergyVad energyVad;
    private boolean closeMic = false;

    private LinkedHashMap<Integer, String> asrMap = new LinkedHashMap<>();
    private int kwsStartTime = 0;

    private String resultKeyWord = "";

    private StringBuilder tempSb = new StringBuilder();

    private String keyWords = "";

    public ASR(String keyWords) {
        this.keyWords = keyWords;
    }

    public ASR() {
    }

    public List<String> listen() {
        StringBuilder stringBuilder = new StringBuilder();
        recorderThread = new Thread(() -> {
            try {
                energizeVad();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        recorderThread.start();
        try {
            recorderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stringBuilder.append(tempSb);
        if (kwsStartTime != 0) {
            for (int key : asrMap.keySet()) {
                if (key < kwsStartTime - 200) stringBuilder.append(asrMap.get(key)).append(" ");
            }
        }
        return List.of(resultKeyWord, stringBuilder.toString().trim());

    }

    private void getAsrResult(int status) {
        if (status == 0) {
            Log.d(TAG, "ASR working");
            while (asrClient.hasResult(true)) {
                String result = asrClient.getResult();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String recogResult = jsonObj.getString("resultText");
                    JSONArray wordsList = jsonObj.getJSONArray("words");
                    for (int key : asrMap.keySet()) {
                        tempSb.append(asrMap.get(key)).append(" ");
                    }
                    asrMap.clear();
                    for (int i = 0; i < wordsList.length(); i++) {
                        JSONObject obj = wordsList.getJSONObject(i);
                        int startTime = obj.getInt("startTimeMsec");
                        String currWord = obj.getString("word");
                        asrMap.put(startTime, currWord);
                    }
                    Log.e(TAG, recogResult);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
        asrClient.waitForCompletion();
        asrClient.destroy();
        Log.d(TAG, "ASR stopped");
    }

    private void getKwsResult(int status) {
        if (status == 0) {
            Log.d(TAG, "KWS working");
            while (kwsClient.hasResult(true)) {
                Log.d(TAG, String.valueOf(java.util.Calendar.getInstance().getTime()));
                String result = kwsClient.getResult();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String recogResult = jsonObj.getString("words");
                    JSONArray list = jsonObj.getJSONArray("words");
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject rec = list.getJSONObject(i);
                        int confindence = (Integer) rec.get("confidence");
                        String kw = rec.getString("word");
                        if (confindence > 60) {
                            resultKeyWord = kw;
                            kwsStartTime = rec.getInt("startTimeMsec");
                            asrClient.endStream();
                            closeMic = true;
                        }
                        Log.e(TAG, String.valueOf(confindence));
                        String word = rec.getString("word");
                        Log.d(TAG, word);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            kwsClient.waitForCompletion();
            kwsClient.destroy();
            Log.d(TAG, "KWS stopped");
        }
    }


    private void energizeVad() throws Exception {
        androidMic = new AndroidMic();
        androidMic.open();
        frameSizeInSamples = (FRAME_RATE * FRAME_MS) / 1000;
        frameSizeInBytes = 2 * frameSizeInSamples;
        energyVad = new EnergyVad(HEAD_MARGIN, TAILMARGIN, frameSizeInBytes);
        energyVad.setCallbacks(this);
        androidMic.start();
        energyVad.process();
        Log.d(TAG, "energizeVad - " + java.util.Calendar.getInstance().getTime());
    }


    @Override
    public int callbackRead(short[] buf, int offset, int len) {
        //read from microphone
        if (closeMic) return -1;
        int currentLen = len * 2;
        byte[] bufferBytes = new byte[currentLen];
        int cnt = 0;
        if (androidMic != null)
            cnt = androidMic.read(bufferBytes, 0, currentLen);
        else
            return -1;
        if (cnt > 0) {
            ByteBuffer in;
            in = ByteBuffer.allocate(currentLen);
            in.put(bufferBytes);
            in.order(ByteOrder.LITTLE_ENDIAN);
            in.rewind();
            if (in.hasArray()) {
                in.asShortBuffer().get(buf, offset, cnt / 2);

            }
        } else if (cnt < 0) {
            return -1;
        }
        return cnt / 2;
    }

    @Override
    public int callbackProcess(short[] buf, int offset, int len) {
        if (closeMic) return -1;
        byte[] data = new byte[len * 2];
        ByteBuffer bf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        bf.asShortBuffer().put(buf, offset, len);
        byte[] leBuffer = bf.array();
        if ((asrClient != null && asrClient.isOk())
                && (kwsClient != null && kwsClient.isOk())) {
            kwsClient.write(leBuffer, 0, leBuffer.length);
            asrClient.write(leBuffer, 0, leBuffer.length);
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public int callbackGetCheck() {
        if (androidMic.isOpen()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public void callbackStart() {
        if (!closeMic) {
            Log.d(TAG, "callbackStart - " + java.util.Calendar.getInstance().getTime());
            kwsClient = new ASRClient(getAsrConf(true));
            asrClient = new ASRClient(getAsrConf(false));
            int kwsStatus = kwsClient.connect();
            int asrStatus = asrClient.connect();
            if (kwsStatus != 0 || asrStatus != 0) {
                androidMic.close();
                return;
            }
            kwsThread = new Thread(() -> getKwsResult(kwsStatus));
            asrThread = new Thread(() -> getAsrResult(asrStatus));
            kwsThread.start();
            asrThread.start();
        }
    }

    @Override
    public void callbackStop() {
        if (closeMic) return;
        Log.d(TAG, "callbackStop - " + java.util.Calendar.getInstance().getTime());
        kwsClient.endStream();
        asrClient.endStream();
        try {
            kwsThread.join();
            asrThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (closeMic) {
            androidMic.close();
        }
    }

    @Override
    public void callbackVisualize(float value) {

    }

    private ASRClientConfiguration getAsrConf(Boolean isKWS) {
        ASRClientConfiguration conf = new ASRClientConfiguration();
        conf.setHost(ASR_HOST);
        conf.setPort(ASR_PORT);
        conf.setAudioSource(AudioSource.AS_REALTIME);
        if (isKWS) {
            conf.setResultType(ResultType.RT_KWS_PARTIAL);
            conf.setCustomDictionary(keyWords);
        } else {
            conf.setResultType(ResultType.RT_PARTIAL_WITH_WORDS);
        }
        conf.setLanguage(AZ_LANG);
        conf.setSSLEnabled(true);
        conf.setOpusEnabled(true);
        return conf;
    }
}
