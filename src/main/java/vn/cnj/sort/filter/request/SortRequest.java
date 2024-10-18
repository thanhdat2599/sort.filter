package vn.cnj.sort.filter.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cnj.sort.filter.enums.sort.filter.SortDirection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    private String key;
    private SortDirection direction;
}