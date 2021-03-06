package com.zedcn.qingstor.api;

import com.zedcn.qingstor.elements.QingStorObject;
import com.zedcn.qingstor.excption.RequestExcption;
import com.zedcn.qingstor.excption.UnauthorizedExcption;
import okhttp3.*;
import okio.BufferedSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 青云对象Api工具类
 * Created by Zed on 2016/8/16.
 */
final class ApiUtils {

    static final String GET = "GET";
    static final String PUT = "PUT";
    static final String DELETE = "DELETE";
    static final String HEAD = "HEAD";

    static QingStorObject buildObject(String key, ResponseBody body) {
        QingStorObject object = new QingStorObject();
        object.setKey(key);
        MediaType mediaType = body.contentType();
        object.setContentType(Objects.nonNull(mediaType) ? mediaType.type() : null);
        object.setContentLength(body.contentLength());
        object.setContent(body.byteStream());
        return object;
    }

    static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    static RequestBody streamBody(long length, InputStream inputStream, MediaType mediaType) {
        return new RequestBody() {
            @Override
            public long contentLength() throws IOException {
                return length;
            }

            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[128];
                while (inputStream.available() > 0) {
                    int len = inputStream.read(buf);
                    baos.write(buf, 0, len);
                }
                sink.write(baos.toByteArray(), 0, baos.size());
            }
        };
    }

    static boolean isEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        return false;
    }

    static void requestAnyway(Request request) {
        OkHttpClient client = getClient();
        try {
            Response response = client.newCall(request).execute();
            switch (response.code()) {
                case 401:
                case 403:
                    throw new UnauthorizedExcption();
            }
        } catch (IOException e) {
            throw new RequestExcption(e);
        }
    }
}
