package vn.cnj.shared.sortfilter.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cnj.shared.sortfilter.enums.sort.filter.SortDirection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    private String key;
    private SortDirection direction;
}