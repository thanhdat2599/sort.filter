package vn.cnj.shared.sortfilter.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cnj.shared.sortfilter.enums.sort.filter.FieldType;
import vn.cnj.shared.sortfilter.enums.sort.filter.Operator;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private String key;

    private Operator operator;

    private FieldType fieldType;

    private Object value;

    private Object valueTo;

    private List<Object> values;
}
