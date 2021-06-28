package az.azreco.simsimapp.azreco.asr;

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

import static az.azreco.simsimapp.constant.AzrecoConstants.ASR_HOST;
import static az.azreco.simsimapp.constant.AzrecoConstants.ASR_PORT;
import static az.azreco.simsimapp.constant.AzrecoConstants.AZ_LANG;

public class KeywordSpotting implements Callbacks {


    private static final int FRAME_MS = 100;
    private static final int FRAME_RATE = 16000;
    private static final int HEAD_MARGIN = 600;
    private static final int TAILMARGIN = 2000;
    private static final String TAG = "KeywordSpotting";

    private Thread recorderThread = null;/* Thread for running the Loop */
    private Thread kwsThread = null;/* Thread for running the Loop */
    private int frameSizeInBytes;
    private int frameSizeInSamples;
    private ASRClient kwsClient = null;
    private AndroidMic androidMic = null;
    private EnergyVad energyVad;
    private boolean closeMic = false;
    private String keyWords = "";

    private int failedTimes = 0;


    private void destroy() {
        recorderThread = null;
        kwsThread = null;
        kwsClient = null;
        androidMic = null;
        closeMic = false;
        failedTimes = 0;
    }

    public void addKeyWords(String keywords) {
        this.keyWords = keywords;
    }

    public KeywordSpotting(String keywords) {
        this.keyWords = keywords;
    }

    public KeywordSpotting() {
    }

    private String keyWord = "";

    public String listen(String keywords) {
        this.keyWords = keywords;
        keyWord = "";
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
        destroy();
        return keyWord;
    }


    private void getKwsResult(int status) {
        if (status == 0) {
            Log.d(TAG, "KWS working");
            while (kwsClient.hasResult(true)) {
                Log.d(TAG, String.valueOf(java.util.Calendar.getInstance().getTime()));
                String result = kwsClient.getResult();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray list = jsonObj.getJSONArray("words");

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject rec = list.getJSONObject(i);
                        int confindence = (Integer) rec.get("confidence");
                        if (confindence > 60) {
                            keyWord = rec.getString("word");
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
        if (kwsClient != null && kwsClient.isOk()) {
            kwsClient.write(leBuffer, 0, leBuffer.length);
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
            kwsClient = new ASRClient(getKwsConf());
            int kwsStatus = kwsClient.connect();
            if (kwsStatus != 0) {
                androidMic.close();
                return;
            }
            kwsThread = new Thread(() -> getKwsResult(kwsStatus));
            kwsThread.start();
        }
    }

    @Override
    public void callbackStop() {
        if (closeMic) return;
        Log.d(TAG, "callbackStop - " + java.util.Calendar.getInstance().getTime());
        kwsClient.endStream();
        try {
            kwsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (keyWord.isEmpty() && failedTimes >= 1) {
            Log.d(TAG, "FAILED TIME " + failedTimes);
            closeMic = true;
            keyWord = "error";
        } else {
            Log.d(TAG, "FAILED TIME " + failedTimes);
            failedTimes += 1;
        }
        if (closeMic) {
            androidMic.close();
        }
    }

    @Override
    public void callbackVisualize(float value) {

    }

    private ASRClientConfiguration getKwsConf() {
        ASRClientConfiguration conf = new ASRClientConfiguration();
        conf.setHost(ASR_HOST);
        conf.setPort(ASR_PORT);
        conf.setAudioSource(AudioSource.AS_REALTIME);
        conf.setResultType(ResultType.RT_KWS_PARTIAL);
        conf.setCustomDictionary(keyWords);
        conf.setLanguage(AZ_LANG);
        conf.setSSLEnabled(true);
        conf.setOpusEnabled(true);
        return conf;
    }

}
