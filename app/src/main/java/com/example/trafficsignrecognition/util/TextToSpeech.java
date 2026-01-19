package com.example.trafficsignrecognition.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;
import com.baidu.paddle.lite.Tensor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextToSpeech {
    private static final String TAG = "TTS";
    protected PaddlePredictor AMPredictor = null;
    protected PaddlePredictor VOCPredictor = null;
    protected float[] wav;
    private final int sampleRate = 24000;
    public boolean isLoaded = false;
    public int cpuThreadNum = 1;
    public String cpuPowerMode = "LITE_POWER_HIGH";

    private final AudioTrack audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
            AudioTrack.MODE_STREAM
            );

    public TextToSpeech(Context context) {
        init(context);
    }
    public void predict(List<Integer> result) throws IOException {
        Log.e(TAG, result.toString());
        // 文本前端
        float[] phones = TextToPhoneConverter(result);
        if (isLoaded() && runModel(phones)) {
            // 播放音频数据
            playAudio(wav);
        } else {
            Log.e(TAG, "Failed to run the speech synthesis model");
        }
    }

    private float[] TextToPhoneConverter(List<Integer> result) {
        List<Float> phonesList = new ArrayList<>(); // 使用 ArrayList 来存储结果
        if (result != null && !result.isEmpty()) {
            // 去重
            result = result.stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < result.size(); i++) {
                float[] floats = ConstantUtil.phoneId[result.get(i)];
                for (float f : floats) {
                    phonesList.add(f); // 将每个 float 添加到列表中
                }
            }
            // 将 List 转换为数组
            float[] phones = new float[phonesList.size()];
            for (int i = 0; i < phonesList.size(); i++) {
                phones[i] = phonesList.get(i);
            }
            Log.e(TAG, "phones生成成功！");
            return phones; // 返回最终的 phones 数组
        }
        return null;
    }


    private void init(Context appCtx){
        // Release model if exists
        releaseModel();
        AMPredictor = loadModel(appCtx,ConstantUtil.AM_MODEL, cpuThreadNum, cpuPowerMode);
        if (AMPredictor == null) {
            return;
        }
        VOCPredictor = loadModel(appCtx,ConstantUtil.VO_MODEL, cpuThreadNum,cpuPowerMode);
        if (VOCPredictor == null) {
            return;
        }
        isLoaded = true;
    }

    protected PaddlePredictor loadModel(Context appCtx, String modelName, int cpuThreadNum, String cpuPowerMode) {
        File modelDirectory = new File(appCtx.getExternalFilesDir(null), "model");
        if (!modelDirectory.exists()) {
            if(modelDirectory.mkdirs()){
                Log.e(TAG, "创建文件夹成功");
            } else {
                Log.e(TAG, "创建文件夹失败");
            }
        }
        // 模型存放路径
        String realPath = modelDirectory.getPath() + File.separator + modelName;
        // 将模型从 assets 中复制到目标目录
        copyFileFromAssets(appCtx, modelName, realPath);

        MobileConfig config = new MobileConfig();
        config.setModelFromFile(realPath);
        Log.e(TAG, "File:" + realPath);
        config.setThreads(cpuThreadNum);
        if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_HIGH")) {
            config.setPowerMode(PowerMode.LITE_POWER_HIGH);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_LOW")) {
            config.setPowerMode(PowerMode.LITE_POWER_LOW);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_FULL")) {
            config.setPowerMode(PowerMode.LITE_POWER_FULL);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_NO_BIND")) {
            config.setPowerMode(PowerMode.LITE_POWER_NO_BIND);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_HIGH")) {
            config.setPowerMode(PowerMode.LITE_POWER_RAND_HIGH);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_LOW")) {
            config.setPowerMode(PowerMode.LITE_POWER_RAND_LOW);
        } else {
            Log.e(TAG, "Unknown cpu power mode!");
            return null;
        }
        return PaddlePredictor.createPaddlePredictor(config);
    }

    public boolean isLoaded() {
        return AMPredictor != null && VOCPredictor != null && isLoaded;
    }

    public boolean runModel(float[] phones) {
        if (!isLoaded() || phones == null) {
            return false;
        }
        //声学模型 非自回归模型：FastSpeech2
        Tensor am_output_handle = getAMOutput(phones, AMPredictor);
        //声码器 非自回归模型：Multi Band MelGAN
        wav = getVOCOutput(am_output_handle, VOCPredictor);
        return true;
    }


    public Tensor getAMOutput(float[] phones, PaddlePredictor am_predictor) {
        Tensor phones_handle = am_predictor.getInput(0);
        long[] dims = {phones.length};
        phones_handle.resize(dims);
        phones_handle.setData(phones);
        am_predictor.run();
        // [?, 80]
        // long outputShape[] = am_output_handle.shape();
        //float[] am_output_data = am_output_handle.getFloatData();
        // [? x 80]
        // long[] am_output_data_shape = {am_output_data.length};
        // Log.e(TAG, Arrays.toString(am_output_data));
        // 打印 mel 数组
        // for (int i=0;i<outputShape[0];i++) {
        //      Log.e(TAG, Arrays.toString(Arrays.copyOfRange(am_output_data,i*80,(i+1)*80)));
        // }
        // voc_predictor 需要知道输入的 shape，所以不能输出转成 float 之后的一维数组
        return am_predictor.getOutput(0);
    }

    public float[] getVOCOutput(Tensor input, PaddlePredictor voc_predictor) {
        Tensor mel_handle = voc_predictor.getInput(0);
        // [?, 80]
        long[] dims = input.shape();
        mel_handle.resize(dims);
        float[] am_output_data = input.getFloatData();
        mel_handle.setData(am_output_data);
        voc_predictor.run();
        Tensor voc_output_handle = voc_predictor.getOutput(0);
        /* [? x 300, 1]
         long[] outputShape = voc_output_handle.shape();
         long[] voc_output_data_shape = {voc_output_data.length};*/
        return voc_output_handle.getFloatData();
    }


    // 从 assets 复制文件的函数
    public void copyFileFromAssets(Context appCtx, String srcPath, String dstPath) {
        if (srcPath.isEmpty() || dstPath.isEmpty()) {
            return;
        }

        // 检查目标文件是否已经存在，如果存在则不复制
        File dstFile = new File(dstPath);
        if (dstFile.exists()) {
            return;  // 文件已存在，不需要复制
        }

        // 如果文件不存在，则进行复制操作
        try (InputStream is = new BufferedInputStream(appCtx.getAssets().open(srcPath));
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(dstFile.toPath()))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseModel() {
        AMPredictor = null;
        VOCPredictor = null;
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
    }
    private void playAudio(float[] data) {
        // Convert float array to short array
        short[] audioData = FloatArray2ShortArray(data);

        // Start playback
        audioTrack.play();
        audioTrack.write(audioData, 0, audioData.length);
    }

    // 停止播放并释放资源
    public void stopPlayback() {
        if (audioTrack != null) {
            // 停止播放
            audioTrack.stop();
        }
    }

    private static short[] FloatArray2ShortArray(float[] values) {
        float max = (float) 0.01;
        short[] ret = new short[values.length];

        for (float value : values) {
            if (Math.abs(value) > max) {
                max = Math.abs(value);
            }
        }

        for (int i = 0; i < values.length; i++) {
            values[i] = values[i] * (32767 / max);
            ret[i] = (short) (values[i]);
        }
        return ret;
    }
}
