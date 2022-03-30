package bside.keepon;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResponseDto {
    private String code;
    private String message;
    private Object data;

    private ResponseDto() {}

    public static ResponseDto getSuccessDto() {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode(ResultType.success.getCode());
        return responseDto;
    }

    public static ResponseDto getSuccessDto(Object data) {
        ResponseDto responseDto = getSuccessDto();
        responseDto.setData(data);
        return responseDto;
    }

    public static ResponseDto getFailDto() {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode(ResultType.fail.getCode());
        return responseDto;
    }

    public static ResponseDto getFailDto(String message) {
        ResponseDto responseDto = getFailDto();
        responseDto.setMessage(message);
        return responseDto;
    }

}