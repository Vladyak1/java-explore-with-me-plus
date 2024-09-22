package ru.practicum.event.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import ru.practicum.event.dto.EventPublicParams;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;

import java.util.ArrayList;
import java.util.List;

public class CustomizedEventStorageImpl implements CustomizedEventStorage {
    @PersistenceContext
    private EntityManager entityManager;
    private final Sort defaultSort = Sort.by(Sort.Direction.ASC, "id");


    @Override
    public List<Event> searchPublicEvents(EventPublicParams param) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        query.select(root)
                .where(buildPublicPredicate(criteriaBuilder, root, param))
                .orderBy(QueryUtils.toOrders(defaultSort, root, criteriaBuilder));

        return entityManager.createQuery(query)
                .setFirstResult(param.getFrom())
                .setMaxResults(param.getSize())
                .getResultList();
    }

    private Predicate[] buildPublicPredicate(CriteriaBuilder cb,
                                             Root<Event> root,
                                             EventPublicParams param) {
        List<Predicate> predicates = new ArrayList<>();
        var initiator = (Join<Object, Object>)root.fetch("initiator");
        var category = (Join<Object, Object>)root.fetch("category");

        // Только опубликованные event
        predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

        // Поиск в соответствии с текстом в аннотации или описании
        Predicate annotationText = cb.like(cb.lower(root.get("annotation")),
                "%" + param.getText().toLowerCase() + "%");
        Predicate descriptionText = cb.like(cb.lower(root.get("description")),
                "%" + param.getText().toLowerCase() + "%");
        predicates.add(cb.or(annotationText, descriptionText));
        if (param.getCategories() != null && !param.getCategories().isEmpty()
                && !(param.getCategories().size() == 1 && param.getCategories().contains(0L))) {
            predicates.add(cb.in(root.get("category").get("id")).value(param.getCategories()));
        }
        if (param.getPaid() != null) {
            predicates.add(cb.equal(root.get("paid"), param.getPaid()));
        }
        if (param.getRangeStart() != null) {
            predicates.add(cb.greaterThan(root.get("eventDate"), param.getRangeStart()));
        }
        if (param.getRangeEnd() != null) {
            predicates.add(cb.lessThan(root.get("eventDate"), param.getRangeEnd()));
        }
        if (param.getOnlyAvailable()) {
            predicates.add(cb.lt(root.get("confirmedRequests"), root.get("participantLimit")));
        }

        return predicates.toArray(new Predicate[0]);
    }

}