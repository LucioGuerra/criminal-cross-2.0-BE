package org.athlium.gym.infrastructure.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionTemplateType;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.gym.infrastructure.document.ActivityScheduleDocument;
import org.athlium.gym.infrastructure.mapper.ActivityScheduleMapper;
import org.bson.Document;

import java.util.Comparator;

import java.util.List;

@ApplicationScoped
public class ActivityScheduleRepositoryImpl implements ActivityScheduleRepository {

    private static final String COUNTERS_COLLECTION = "counters";
    private static final String COUNTER_ID = "activity_schedule_id";

    @Inject
    ActivitySchedulePanacheRepository panacheRepository;

    @Inject
    ActivityScheduleMapper mapper;

    @Inject
    MongoClient mongoClient;

    private String databaseName;

    @PostConstruct
    void init() {
        databaseName = panacheRepository.mongoCollection().getNamespace().getDatabaseName();
        panacheRepository.mongoCollection().createIndex(new Document("scheduleId", 1));
    }

    @Override
    public ActivitySchedule save(ActivitySchedule schedule) {
        ActivityScheduleDocument managedDocument = null;
        if (schedule.getId() != null) {
            managedDocument = panacheRepository.find("scheduleId", schedule.getId()).firstResult();
        }

        if (managedDocument != null) {
            managedDocument.organizationId = schedule.getOrganizationId();
            managedDocument.headquartersId = schedule.getHeadquartersId();
            managedDocument.activityId = schedule.getActivityId();
            managedDocument.dayOfWeek = schedule.getDayOfWeek();
            managedDocument.weekDays = schedule.getWeekDays();
            managedDocument.startTime = schedule.getStartTime();
            managedDocument.durationMinutes = schedule.getDurationMinutes();
            managedDocument.active = schedule.getActive();
            managedDocument.schedulerType = schedule.getSchedulerType();
            managedDocument.activeFrom = schedule.getActiveFrom();
            managedDocument.activeUntil = schedule.getActiveUntil();
            managedDocument.scheduledDate = schedule.getScheduledDate();
            return toDomain(managedDocument);
        }

        ActivityScheduleDocument document = mapper.toDocument(schedule);
        document.scheduleId = nextScheduleId();
        if (document.schedulerType == null) {
            document.schedulerType = SchedulerType.WEEKLY_RANGE;
        }

        panacheRepository.persist(document);
        return toDomain(document);
    }

    @Override
    public List<ActivitySchedule> findAllActive() {
        return toDomainList(panacheRepository.find("active", true).list());
    }

    @Override
    public List<ActivitySchedule> findByHeadquartersId(Long headquartersId) {
        return toDomainList(panacheRepository.find("headquartersId = ?1 and active = ?2", headquartersId, true).list());
    }

    private List<ActivitySchedule> toDomainList(List<ActivityScheduleDocument> documents) {
        return documents.stream()
                .sorted(Comparator.comparing(doc -> doc.scheduleId))
                .map(this::toDomain)
                .toList();
    }

    private ActivitySchedule toDomain(ActivityScheduleDocument document) {
        ActivitySchedule schedule = mapper.toDomain(document);
        schedule.setId(document.scheduleId);
        if (document.schedulerType == SchedulerType.ONE_TIME_DISPOSABLE) {
            schedule.setTemplateType(SessionTemplateType.ONE_TIME_DISPOSABLE);
        } else {
            schedule.setTemplateType(SessionTemplateType.WEEKLY_RANGE);
        }
        return schedule;
    }

    private Long nextScheduleId() {
        Document result = mongoClient.getDatabase(databaseName)
                .getCollection(COUNTERS_COLLECTION)
                .findOneAndUpdate(
                        Filters.eq("_id", COUNTER_ID),
                        Updates.inc("sequence", 1L),
                        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
                );

        if (result == null || result.get("sequence") == null) {
            throw new IllegalStateException("Unable to generate activity schedule id");
        }

        return ((Number) result.get("sequence")).longValue();
    }
}
