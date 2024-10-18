Common sort filter for springboot

How to use: 

public Page<LogDto> search(SearchRequest request) {
        Specification<Log> specification = new SearchSpecification<>(request);
        Pageable pageable = SearchSpecification.getPageable(request.getPage(), request.getSize());
        Page<Log> entities = logRepository.findAll(specification, pageable);
        return entities.map(LogMapper.INSTANCE::toDto);
}
