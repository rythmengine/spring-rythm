package org.rythmengine.spring.web.result;

import org.osgl.util.E;
import org.osgl.util.IO;
import org.osgl.util.S;
import org.osgl.web.util.UserAgent;
import org.rythmengine.spring.web.UADetector;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JSONResult extends Result {

    private String json;

    private Map<String, Object> objects;

    private Object object;

    private Object[] array;

    public JSONResult(String json) {
        super(HttpStatus.OK);
        E.illegalArgumentIf(S.blank(json));
        this.json = json;
    }

    public JSONResult(Map<String, Object> objects) {
        super(HttpStatus.OK);
        E.NPE(objects);
        this.objects = objects;
    }

    public JSONResult(Object object) {
        super(HttpStatus.OK);
        E.NPE(object);
        this.object = object;
    }

    public JSONResult(Object... array) {
        super(HttpStatus.OK);
        E.NPE(array);
        this.array = array;
    }

    @Override
    protected ModelAndView writeToResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        UserAgent ua = UADetector.get();
        response.setContentType(null != ua && ua.isIE9Down() ? "text/plain" : "application/json");
        if (null != objects) {
            json = com.alibaba.fastjson.JSON.toJSONString(objects);
        } else if (null != object) {
            json = com.alibaba.fastjson.JSON.toJSONString(object);
        } else if (null != array) {
            json = com.alibaba.fastjson.JSON.toJSONString(array);
        }
        IO.writeContent(json, response.getWriter());
        return new ModelAndView();
    }
}
