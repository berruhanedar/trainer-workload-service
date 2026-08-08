package com.berruhanedar.app.mapper;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.entity.TrainerWorkload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "years", ignore = true)
    TrainerWorkload toEntity(TrainerWorkloadRequestDto request);
}