package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanAllocationExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanAllocationExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIsNull() {
            addCriterion("test_plan_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIsNotNull() {
            addCriterion("test_plan_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdEqualTo(String value) {
            addCriterion("test_plan_id =", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotEqualTo(String value) {
            addCriterion("test_plan_id <>", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThan(String value) {
            addCriterion("test_plan_id >", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_id >=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThan(String value) {
            addCriterion("test_plan_id <", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_id <=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLike(String value) {
            addCriterion("test_plan_id like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotLike(String value) {
            addCriterion("test_plan_id not like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIn(List<String> values) {
            addCriterion("test_plan_id in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotIn(List<String> values) {
            addCriterion("test_plan_id not in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdBetween(String value1, String value2) {
            addCriterion("test_plan_id between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_id not between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdIsNull() {
            addCriterion("test_resource_pool_id is null");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdIsNotNull() {
            addCriterion("test_resource_pool_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdEqualTo(String value) {
            addCriterion("test_resource_pool_id =", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotEqualTo(String value) {
            addCriterion("test_resource_pool_id <>", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdGreaterThan(String value) {
            addCriterion("test_resource_pool_id >", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_resource_pool_id >=", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLessThan(String value) {
            addCriterion("test_resource_pool_id <", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLessThanOrEqualTo(String value) {
            addCriterion("test_resource_pool_id <=", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLike(String value) {
            addCriterion("test_resource_pool_id like", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotLike(String value) {
            addCriterion("test_resource_pool_id not like", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdIn(List<String> values) {
            addCriterion("test_resource_pool_id in", values, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotIn(List<String> values) {
            addCriterion("test_resource_pool_id not in", values, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdBetween(String value1, String value2) {
            addCriterion("test_resource_pool_id between", value1, value2, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotBetween(String value1, String value2) {
            addCriterion("test_resource_pool_id not between", value1, value2, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIsNull() {
            addCriterion("retry_on_fail is null");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIsNotNull() {
            addCriterion("retry_on_fail is not null");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailEqualTo(Boolean value) {
            addCriterion("retry_on_fail =", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotEqualTo(Boolean value) {
            addCriterion("retry_on_fail <>", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailGreaterThan(Boolean value) {
            addCriterion("retry_on_fail >", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailGreaterThanOrEqualTo(Boolean value) {
            addCriterion("retry_on_fail >=", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailLessThan(Boolean value) {
            addCriterion("retry_on_fail <", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailLessThanOrEqualTo(Boolean value) {
            addCriterion("retry_on_fail <=", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIn(List<Boolean> values) {
            addCriterion("retry_on_fail in", values, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotIn(List<Boolean> values) {
            addCriterion("retry_on_fail not in", values, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailBetween(Boolean value1, Boolean value2) {
            addCriterion("retry_on_fail between", value1, value2, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotBetween(Boolean value1, Boolean value2) {
            addCriterion("retry_on_fail not between", value1, value2, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIsNull() {
            addCriterion("retry_type is null");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIsNotNull() {
            addCriterion("retry_type is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTypeEqualTo(String value) {
            addCriterion("retry_type =", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotEqualTo(String value) {
            addCriterion("retry_type <>", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeGreaterThan(String value) {
            addCriterion("retry_type >", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeGreaterThanOrEqualTo(String value) {
            addCriterion("retry_type >=", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeLessThan(String value) {
            addCriterion("retry_type <", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeLessThanOrEqualTo(String value) {
            addCriterion("retry_type <=", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeLike(String value) {
            addCriterion("retry_type like", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotLike(String value) {
            addCriterion("retry_type not like", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIn(List<String> values) {
            addCriterion("retry_type in", values, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotIn(List<String> values) {
            addCriterion("retry_type not in", values, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeBetween(String value1, String value2) {
            addCriterion("retry_type between", value1, value2, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotBetween(String value1, String value2) {
            addCriterion("retry_type not between", value1, value2, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNull() {
            addCriterion("retry_times is null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNotNull() {
            addCriterion("retry_times is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesEqualTo(Integer value) {
            addCriterion("retry_times =", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotEqualTo(Integer value) {
            addCriterion("retry_times <>", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThan(Integer value) {
            addCriterion("retry_times >", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_times >=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThan(Integer value) {
            addCriterion("retry_times <", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThanOrEqualTo(Integer value) {
            addCriterion("retry_times <=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIn(List<Integer> values) {
            addCriterion("retry_times in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotIn(List<Integer> values) {
            addCriterion("retry_times not in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesBetween(Integer value1, Integer value2) {
            addCriterion("retry_times between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_times not between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNull() {
            addCriterion("retry_interval is null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNotNull() {
            addCriterion("retry_interval is not null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalEqualTo(Integer value) {
            addCriterion("retry_interval =", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotEqualTo(Integer value) {
            addCriterion("retry_interval <>", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThan(Integer value) {
            addCriterion("retry_interval >", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_interval >=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThan(Integer value) {
            addCriterion("retry_interval <", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThanOrEqualTo(Integer value) {
            addCriterion("retry_interval <=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIn(List<Integer> values) {
            addCriterion("retry_interval in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotIn(List<Integer> values) {
            addCriterion("retry_interval not in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalBetween(Integer value1, Integer value2) {
            addCriterion("retry_interval between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_interval not between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIsNull() {
            addCriterion("stop_on_fail is null");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIsNotNull() {
            addCriterion("stop_on_fail is not null");
            return (Criteria) this;
        }

        public Criteria andStopOnFailEqualTo(Boolean value) {
            addCriterion("stop_on_fail =", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotEqualTo(Boolean value) {
            addCriterion("stop_on_fail <>", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailGreaterThan(Boolean value) {
            addCriterion("stop_on_fail >", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailGreaterThanOrEqualTo(Boolean value) {
            addCriterion("stop_on_fail >=", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailLessThan(Boolean value) {
            addCriterion("stop_on_fail <", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailLessThanOrEqualTo(Boolean value) {
            addCriterion("stop_on_fail <=", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIn(List<Boolean> values) {
            addCriterion("stop_on_fail in", values, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotIn(List<Boolean> values) {
            addCriterion("stop_on_fail not in", values, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_on_fail between", value1, value2, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_on_fail not between", value1, value2, "stopOnFail");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}