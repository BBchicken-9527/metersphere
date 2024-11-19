package io.metersphere.dashboard.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateDTO;
import io.metersphere.api.dto.definition.ApiRefSourceCountDTO;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.bug.service.BugStatusService;
import io.metersphere.dashboard.constants.DashboardUserLayoutKeys;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.dto.NameArrayDTO;
import io.metersphere.dashboard.dto.NameCountDTO;
import io.metersphere.dashboard.dto.StatusPercentDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.dto.FunctionalCaseStatisticDTO;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.project.dto.ProjectUserStatusCountDTO;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ExtProjectMemberMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserLayout;
import io.metersphere.system.domain.UserLayoutExample;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.ProjectUserMemberDTO;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.mapper.UserLayoutMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.dashboard.result.DashboardResultCode.NO_PROJECT_PERMISSION;

/**
 * @author guoyuqi
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardService {
    @Resource
    private DashboardProjectService dashboardProjectService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ExtBugMapper extBugMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private ExtProjectMemberMapper extProjectMemberMapper;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private PermissionCheckService permissionCheckService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserLayoutMapper userLayoutMapper;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private CaseReviewService caseReviewService;
    @Resource
    private ApiTestService apiTestService;


    public static final String FUNCTIONAL = "FUNCTIONAL"; // 功能用例
    public static final String CASE_REVIEW = "CASE_REVIEW"; // 用例评审
    public static final String API = "API";
    public static final String API_CASE = "API_CASE";
    public static final String API_SCENARIO = "API_SCENARIO";
    public static final String TEST_PLAN = "TEST_PLAN"; // 测试计划
    public static final String BUG_COUNT = "BUG_COUNT"; // 缺陷数量

    public static final String API_TEST_MODULE = "apiTest";
    public static final String TEST_PLAN_MODULE = "testPlan";
    public static final String FUNCTIONAL_CASE_MODULE = "caseManagement";
    public static final String BUG_MODULE = "bugManagement";


    public OverViewCountDTO createByMeCount(DashboardFrontPageRequest request, String userId) {
        OverViewCountDTO map = getNoProjectData(request);
        if (map != null) return map;
        List<Project> projects;
        if (CollectionUtils.isNotEmpty(request.getProjectIds())) {
            projects = extProjectMapper.getProjectNameModule(null, request.getProjectIds());
        } else {
            projects = extProjectMapper.getProjectNameModule(request.getOrganizationId(), null);
        }
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getPermissionModuleProjectIds(projects, userId);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        return getModuleCountMap(permissionModuleProjectIdMap, projects, toStartTime, toEndTime, userId);
    }

    @Nullable
    private static OverViewCountDTO getNoProjectData(DashboardFrontPageRequest request) {
        if (!request.isSelectAll() && CollectionUtils.isEmpty(request.getProjectIds())) {
            Map<String, Integer> map = new HashMap<>();
            map.put(FUNCTIONAL, 0);
            map.put(CASE_REVIEW, 0);
            map.put(API, 0);
            map.put(API_CASE, 0);
            map.put(API_SCENARIO, 0);
            map.put(TEST_PLAN, 0);
            map.put(BUG_COUNT, 0);
            return new OverViewCountDTO(map, new ArrayList<>(), new ArrayList<>(), 0);
        }
        return null;
    }

    @NotNull
    private OverViewCountDTO getModuleCountMap(Map<String, Set<String>> permissionModuleProjectIdMap, List<Project> projects, Long toStartTime, Long toEndTime, String userId) {
        Map<String, Integer> map = new HashMap<>();
        List<String> xaxis = new ArrayList<>();
        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();
        //功能用例
        Map<String, ProjectCountDTO> caseProjectCount = new HashMap<>();
        Set<String> caseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.FUNCTIONAL_CASE_READ);
        if (CollectionUtils.isNotEmpty(caseProjectIds)) {
            //有权限
            List<ProjectCountDTO> projectCaseCount = extFunctionalCaseMapper.projectCaseCount(caseProjectIds, toStartTime, toEndTime, userId);
            int caseCount = projectCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(FUNCTIONAL, caseCount);
            xaxis.add(FUNCTIONAL);
            caseProjectCount = projectCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }

        //用例评审
        Map<String, ProjectCountDTO> reviewProjectCount = new HashMap<>();
        Set<String> reviewProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.CASE_REVIEW_READ);
        if (CollectionUtils.isNotEmpty(reviewProjectIds)) {
            List<ProjectCountDTO> projectReviewCount = extCaseReviewMapper.projectReviewCount(reviewProjectIds, toStartTime, toEndTime, userId);
            int reviewCount = projectReviewCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(CASE_REVIEW, reviewCount);
            xaxis.add(CASE_REVIEW);
            reviewProjectCount = projectReviewCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }
        //接口
        Map<String, ProjectCountDTO> apiProjectCount = new HashMap<>();
        Set<String> apiProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
        if (CollectionUtils.isNotEmpty(apiProjectIds)) {
            List<ProjectCountDTO> projectApiCount = extApiDefinitionMapper.projectApiCount(apiProjectIds, toStartTime, toEndTime, userId);
            int apiCount = projectApiCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API, apiCount);
            xaxis.add(API);
            apiProjectCount = projectApiCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //接口用例
        Map<String, ProjectCountDTO> apiCaseProjectCount = new HashMap<>();
        Set<String> apiCaseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
        if (CollectionUtils.isNotEmpty(apiCaseProjectIds)) {
            List<ProjectCountDTO> projectApiCaseCount = extApiTestCaseMapper.projectApiCaseCount(apiCaseProjectIds, toStartTime, toEndTime, userId);
            int apiCaseCount = projectApiCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_CASE, apiCaseCount);
            xaxis.add(API_CASE);
            apiCaseProjectCount = projectApiCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //接口场景
        Map<String, ProjectCountDTO> apiScenarioProjectCount = new HashMap<>();
        Set<String> scenarioProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
        if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
            List<ProjectCountDTO> projectApiScenarioCount = extApiScenarioMapper.projectApiScenarioCount(scenarioProjectIds, toStartTime, toEndTime, userId);
            int apiScenarioCount = projectApiScenarioCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_SCENARIO, apiScenarioCount);
            xaxis.add(API_SCENARIO);
            apiScenarioProjectCount = projectApiScenarioCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }
        //测试计划
        Map<String, ProjectCountDTO> testPlanProjectCount = new HashMap<>();
        Set<String> planProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.TEST_PLAN_READ);
        if (CollectionUtils.isNotEmpty(planProjectIds)) {
            List<ProjectCountDTO> projectPlanCount = extTestPlanMapper.projectPlanCount(planProjectIds, toStartTime, toEndTime, userId);
            int testPlanCount = projectPlanCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(TEST_PLAN, testPlanCount);
            xaxis.add(TEST_PLAN);
            testPlanProjectCount = projectPlanCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //缺陷管理
        Map<String, ProjectCountDTO> bugProjectCount = new HashMap<>();
        Set<String> bugProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_BUG_READ);
        if (CollectionUtils.isNotEmpty(bugProjectIds)) {
            List<ProjectCountDTO> projectBugCount = extBugMapper.projectBugCount(bugProjectIds, toStartTime, toEndTime, userId);
            int bugCount = projectBugCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(BUG_COUNT, bugCount);
            xaxis.add(BUG_COUNT);
            bugProjectCount = projectBugCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }

        for (Project project : projects) {
            String projectId = project.getId();
            String projectName = project.getName();
            NameArrayDTO nameArrayDTO = new NameArrayDTO();
            nameArrayDTO.setId(projectId);
            nameArrayDTO.setName(projectName);
            List<Integer> count = new ArrayList<>();
            ProjectCountDTO projectCountDTO = caseProjectCount.get(projectId);
            if (projectCountDTO != null) {
                count.add(projectCountDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO reviewDTO = reviewProjectCount.get(projectId);
            if (reviewDTO != null) {
                count.add(reviewDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiDTO = apiProjectCount.get(projectId);
            if (apiDTO != null) {
                count.add(apiDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiCaseDTO = apiCaseProjectCount.get(projectId);
            if (apiCaseDTO != null) {
                count.add(apiCaseDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiScenarioDTO = apiScenarioProjectCount.get(projectId);
            if (apiScenarioDTO != null) {
                count.add(apiScenarioDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO testPlanDTO = testPlanProjectCount.get(projectId);
            if (testPlanDTO != null) {
                count.add(testPlanDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO bugDTO = bugProjectCount.get(projectId);
            if (bugDTO != null) {
                count.add(bugDTO.getCount());
            } else {
                count.add(0);
            }
            nameArrayDTO.setCount(count);
            nameArrayDTOList.add(nameArrayDTO);
        }
        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        overViewCountDTO.setCaseCountMap(map);
        overViewCountDTO.setXAxis(xaxis);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        if (CollectionUtils.isEmpty(xaxis)) {
            overViewCountDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
        }
        return overViewCountDTO;
    }

    public OverViewCountDTO projectViewCount(DashboardFrontPageRequest request, String userId) {
        OverViewCountDTO map = getNoProjectData(request);
        if (map != null) return map;
        List<Project> collect = getHasPermissionProjects(request, userId);
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getModuleProjectIds(collect);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        return getModuleCountMap(permissionModuleProjectIdMap, collect, toStartTime, toEndTime, null);
    }

    private List<Project> getHasPermissionProjects(DashboardFrontPageRequest request, String userId) {
        List<Project> userProject = projectService.getUserProject(request.getOrganizationId(), userId);
        List<Project> collect;
        if (CollectionUtils.isNotEmpty(request.getProjectIds())) {
            collect = userProject.stream().filter(t -> request.getProjectIds().contains(t.getId())).toList();
        } else {
            collect = userProject;
        }
        return collect;
    }


    public List<LayoutDTO> editLayout(String organizationId, String userId, List<LayoutDTO> layoutDTO) {
        UserLayoutExample userLayoutExample = new UserLayoutExample();
        userLayoutExample.createCriteria().andUserIdEqualTo(userId).andOrgIdEqualTo(organizationId);
        List<UserLayout> userLayouts = userLayoutMapper.selectByExample(userLayoutExample);
        Map<String, List<LayoutDTO>> getKeyMap = layoutDTO.stream().collect(Collectors.groupingBy(LayoutDTO::getKey));
        List<LayoutDTO> saveList = new ArrayList<>();
        getKeyMap.forEach((k, v) -> saveList.add(v.get(0)));
        UserLayout userLayout = new UserLayout();
        userLayout.setUserId(userId);
        userLayout.setOrgId(organizationId);
        if (CollectionUtils.isEmpty(saveList)) {
            userLayout.setConfiguration(new byte[0]);
        } else {
            String configuration = JSON.toJSONString(saveList);
            userLayout.setConfiguration(configuration.getBytes());
        }
        if (CollectionUtils.isEmpty(userLayouts)) {
            userLayout.setId(IDGenerator.nextStr());
            userLayoutMapper.insert(userLayout);
        } else {
            userLayout.setId(userLayouts.getFirst().getId());
            userLayoutMapper.updateByPrimaryKeyWithBLOBs(userLayout);
        }
        return layoutDTO;
    }

    public List<LayoutDTO> getLayout(String organizationId, String userId) {
        UserLayoutExample userLayoutExample = new UserLayoutExample();
        userLayoutExample.createCriteria().andUserIdEqualTo(userId).andOrgIdEqualTo(organizationId);
        List<UserLayout> userLayouts = userLayoutMapper.selectByExampleWithBLOBs(userLayoutExample);
        List<Project>allPermissionProjects = extProjectMapper.getProjectNameModule(organizationId, null);
        if (CollectionUtils.isEmpty(allPermissionProjects)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(userLayouts)) {
            return getDefaultLayoutDTOS(allPermissionProjects.getFirst().getId());
        }
        UserLayout userLayout = userLayouts.getFirst();
        byte[] configuration = userLayout.getConfiguration();
        String layoutDTOStr = new String(configuration);
        List<LayoutDTO> layoutDTOS = JSON.parseArray(layoutDTOStr, LayoutDTO.class);
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getPermissionModuleProjectIds(allPermissionProjects, userId);
        List<ProjectUserMemberDTO> orgProjectMemberList = extProjectMemberMapper.getOrgProjectMemberList(organizationId, null);
        rebuildLayouts(layoutDTOS, allPermissionProjects, orgProjectMemberList, permissionModuleProjectIdMap);
        return layoutDTOS.stream().sorted(Comparator.comparing(LayoutDTO::getPos)).collect(Collectors.toList());
    }

    /**
     * 过滤用户在当前项目是否有移除或者项目是否被禁用以及用户是否被删除禁用
     *
     * @param layoutDTOS                   用户保存的布局
     * @param allPermissionProjects        用户有任意权限的所有在役项目
     * @param orgProjectMemberList         当前组织下所有有项目权限的成员
     * @param permissionModuleProjectIdMap 只读权限对应的开启模块的项目ids
     */
    private void rebuildLayouts(List<LayoutDTO> layoutDTOS, List<Project> allPermissionProjects, List<ProjectUserMemberDTO> orgProjectMemberList, Map<String, Set<String>> permissionModuleProjectIdMap) {
        for (LayoutDTO layoutDTO : layoutDTOS) {
            if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_VIEW.toString()) || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CREATE_BY_ME.toString())) {
                if (CollectionUtils.isEmpty(layoutDTO.getProjectIds())) {
                    layoutDTO.setProjectIds(new ArrayList<>());
                } else {
                    List<Project> list = allPermissionProjects.stream().filter(t -> layoutDTO.getProjectIds().contains(t.getId())).toList();
                    if (CollectionUtils.isNotEmpty(list)) {
                        layoutDTO.setProjectIds(list.stream().map(Project::getId).toList());
                    } else {
                        layoutDTO.setProjectIds(allPermissionProjects.stream().map(Project::getId).toList());
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW.toString())) {
                List<ProjectUserMemberDTO> list = orgProjectMemberList.stream().filter(t -> layoutDTO.getHandleUsers().contains(t.getId())).toList();
                layoutDTO.setHandleUsers(list.stream().map(ProjectUserMemberDTO::getId).toList());
                List<Project> projectList = allPermissionProjects.stream().filter(t -> layoutDTO.getProjectIds().contains(t.getId())).toList();
                if (CollectionUtils.isEmpty(projectList)) {
                    layoutDTO.setProjectIds(List.of(allPermissionProjects.getFirst().getId()));
                } else {
                    layoutDTO.setProjectIds(List.of(projectList.getFirst().getId()));
                }
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.ASSOCIATE_CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.REVIEW_CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.REVIEWING_BY_ME.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.FUNCTIONAL_CASE_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_CHANGE.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_CASE_COUNT.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.SCENARIO_COUNT.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.TEST_PLAN_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PLAN_LEGACY_BUG.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.TEST_PLAN_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.BUG_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CREATE_BUG_BY_ME.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.HANDLE_BUG_BY_ME.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.BUG_HANDLE_USER.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_BUG_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            }
        }
    }

    private void checkHasPermissionProject(LayoutDTO layoutDTO, Set<String> hasReadProjectIds) {
        if (CollectionUtils.isEmpty(hasReadProjectIds)) {
            return;
        }
        List<String> projectIds = hasReadProjectIds.stream().filter(t -> layoutDTO.getProjectIds().contains(t)).toList();
        if (CollectionUtils.isEmpty(projectIds)) {
            layoutDTO.setProjectIds(List.of(new ArrayList<>(hasReadProjectIds).getFirst()));
        } else {
            layoutDTO.setProjectIds(List.of(projectIds.getFirst()));
        }
    }

    /**
     * 获取默认布局
     *
     * @param organizationId 组织ID
     * @return List<LayoutDTO>
     */
    private static List<LayoutDTO> getDefaultLayoutDTOS(String organizationId) {
        List<LayoutDTO> layoutDTOS = new ArrayList<>();
        LayoutDTO projectLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_VIEW, "workbench.homePage.projectOverview", 0, new ArrayList<>());
        layoutDTOS.add(projectLayoutDTO);
        LayoutDTO createByMeLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.CREATE_BY_ME, "workbench.homePage.createdByMe", 1, new ArrayList<>());
        layoutDTOS.add(createByMeLayoutDTO);
        LayoutDTO projectMemberLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW, "workbench.homePage.staffOverview", 2, List.of(organizationId));
        layoutDTOS.add(projectMemberLayoutDTO);
        return layoutDTOS;
    }

    /**
     * 构建默认布局内容
     *
     * @param layoutKey  布局卡片的key
     * @param label      布局卡片页面显示的label
     * @param pos        布局卡片 排序
     * @param projectIds 布局卡片所选的项目ids
     * @return LayoutDTO
     */
    private static LayoutDTO buildDefaultLayoutDTO(DashboardUserLayoutKeys layoutKey, String label, int pos, List<String> projectIds) {
        LayoutDTO layoutDTO = new LayoutDTO();
        layoutDTO.setId(UUID.randomUUID().toString());
        layoutDTO.setKey(layoutKey.toString());
        layoutDTO.setLabel(label);
        layoutDTO.setPos(pos);
        layoutDTO.setSelectAll(true);
        layoutDTO.setFullScreen(true);
        layoutDTO.setProjectIds(projectIds);
        layoutDTO.setHandleUsers(new ArrayList<>());
        return layoutDTO;
    }

    public OverViewCountDTO projectMemberViewCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<String> moduleIds = JSON.parseArray(project.getModuleSetting(), String.class);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<ProjectUserMemberDTO> projectMemberList = extProjectMemberMapper.getProjectMemberList(projectId, request.getHandleUsers());
        Map<String, String> userNameMap = projectMemberList.stream().collect(Collectors.toMap(ProjectUserMemberDTO::getId, ProjectUserMemberDTO::getName));
        return getUserCountDTO(userNameMap, moduleIds, projectId, toStartTime, toEndTime);

    }

    @NotNull
    private OverViewCountDTO getUserCountDTO(Map<String, String> userNameMap, List<String> moduleIds, String projectId, Long toStartTime, Long toEndTime) {
        List<String> xaxis = new ArrayList<>(userNameMap.values());
        Set<String> userIds = userNameMap.keySet();
        Map<String, Integer> userCaseCountMap;
        Map<String, Integer> userReviewCountMap;
        Map<String, Integer> userApiCountMap;
        Map<String, Integer> userApiScenarioCountMap;
        Map<String, Integer> userApiCaseCountMap;
        Map<String, Integer> userPlanCountMap;
        Map<String, Integer> userBugCountMap;

        if (moduleIds.contains(FUNCTIONAL_CASE_MODULE)) {
            List<ProjectUserCreateCount> userCreateCaseCount = extFunctionalCaseMapper.userCreateCaseCount(projectId, toStartTime, toEndTime, userIds);
            userCaseCountMap = userCreateCaseCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateReviewCount = extCaseReviewMapper.userCreateReviewCount(projectId, toStartTime, toEndTime, userIds);
            userReviewCountMap = userCreateReviewCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userReviewCountMap = new HashMap<>();
            userCaseCountMap = new HashMap<>();
        }
        if (moduleIds.contains(API_TEST_MODULE)) {
            List<ProjectUserCreateCount> userCreateApiCount = extApiDefinitionMapper.userCreateApiCount(projectId, toStartTime, toEndTime, userIds);
            userApiCountMap = userCreateApiCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateApiScenarioCount = extApiScenarioMapper.userCreateApiScenarioCount(projectId, toStartTime, toEndTime, userIds);
            userApiScenarioCountMap = userCreateApiScenarioCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateApiCaseCount = extApiTestCaseMapper.userCreateApiCaseCount(projectId, toStartTime, toEndTime, userIds);
            userApiCaseCountMap = userCreateApiCaseCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userApiCountMap = new HashMap<>();
            userApiScenarioCountMap = new HashMap<>();
            userApiCaseCountMap = new HashMap<>();
        }
        if (moduleIds.contains(TEST_PLAN_MODULE)) {
            List<ProjectUserCreateCount> userCreatePlanCount = extTestPlanMapper.userCreatePlanCount(projectId, toStartTime, toEndTime, userIds);
            userPlanCountMap = userCreatePlanCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userPlanCountMap = new HashMap<>();
        }
        if (moduleIds.contains(BUG_MODULE)) {
            List<ProjectUserCreateCount> userCreateBugCount = extBugMapper.userCreateBugCount(projectId, toStartTime, toEndTime, userIds);
            userBugCountMap = userCreateBugCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userBugCountMap = new HashMap<>();
        }

        List<Integer> userCaseCount = new ArrayList<>();
        List<Integer> userReviewCount = new ArrayList<>();
        List<Integer> userApiCount = new ArrayList<>();
        List<Integer> userApiCaseCount = new ArrayList<>();
        List<Integer> userApiScenarioCount = new ArrayList<>();
        List<Integer> userPlanCount = new ArrayList<>();
        List<Integer> userBugCount = new ArrayList<>();

        userNameMap.forEach((id, userName) -> {
            if (userCaseCountMap.get(id) != null) {
                userCaseCount.add(userCaseCountMap.get(id));
            } else {
                userCaseCount.add(0);
            }
            if (userReviewCountMap.get(id) != null) {
                userReviewCount.add(userCaseCountMap.get(id));
            } else {
                userReviewCount.add(0);
            }
            if (userApiCountMap.get(id) != null) {
                userApiCount.add(userApiCountMap.get(id));
            } else {
                userApiCount.add(0);
            }
            if (userApiCaseCountMap.get(id) != null) {
                userApiCaseCount.add(userApiCaseCountMap.get(id));
            } else {
                userApiCaseCount.add(0);
            }
            if (userApiScenarioCountMap.get(id) != null) {
                userApiScenarioCount.add(userApiScenarioCountMap.get(id));
            } else {
                userApiScenarioCount.add(0);
            }
            if (userPlanCountMap.get(id) != null) {
                userPlanCount.add(userPlanCountMap.get(id));
            } else {
                userPlanCount.add(0);
            }
            if (userBugCountMap.get(id) != null) {
                userBugCount.add(userBugCountMap.get(id));
            } else {
                userBugCount.add(0);
            }
        });
        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();
        NameArrayDTO userCaseArray = new NameArrayDTO();
        userCaseArray.setCount(userCaseCount);
        nameArrayDTOList.add(userCaseArray);

        NameArrayDTO userReviewArray = new NameArrayDTO();
        userReviewArray.setCount(userReviewCount);
        nameArrayDTOList.add(userReviewArray);

        NameArrayDTO userApiArray = new NameArrayDTO();
        userApiArray.setCount(userApiCount);
        nameArrayDTOList.add(userApiArray);

        NameArrayDTO userApiCaseArray = new NameArrayDTO();
        userApiCaseArray.setCount(userApiCaseCount);
        nameArrayDTOList.add(userApiCaseArray);

        NameArrayDTO userApiScenarioArray = new NameArrayDTO();
        userApiScenarioArray.setCount(userApiScenarioCount);
        nameArrayDTOList.add(userApiScenarioArray);

        NameArrayDTO userPlanArray = new NameArrayDTO();
        userPlanArray.setCount(userPlanCount);
        nameArrayDTOList.add(userPlanArray);

        NameArrayDTO userBugArray = new NameArrayDTO();
        userBugArray.setCount(userBugCount);
        nameArrayDTOList.add(userBugArray);

        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        overViewCountDTO.setXAxis(xaxis);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        if (CollectionUtils.isEmpty(xaxis)) {
            overViewCountDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
        }
        return overViewCountDTO;
    }


    public StatisticsDTO projectCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> allStatisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, null, null);
        buildStatusPercentList(allStatisticListByProjectId, statusPercentList);
        statisticsDTO.setStatusPercentList(statusPercentList);
        Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap = allStatisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getReviewStatus));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        List<NameCountDTO> reviewList = getReviewList(reviewStatusMap, allStatisticListByProjectId);
        statusStatisticsMap.put("review", reviewList);
        List<NameCountDTO> passList = getPassList(reviewStatusMap, allStatisticListByProjectId);
        statusStatisticsMap.put("pass", passList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    @NotNull
    private static List<NameCountDTO> getPassList(Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap, List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<NameCountDTO> passList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> hasPassList = reviewStatusMap.get(FunctionalCaseReviewStatus.PASS.toString());
        if (CollectionUtils.isEmpty(hasPassList)) {
            hasPassList = new ArrayList<>();
        }
        NameCountDTO passRate = new NameCountDTO();
        passRate.setName(Translator.get("functional_case.passRate"));
        if (CollectionUtils.isNotEmpty(statisticListByProjectId)) {
            BigDecimal divide = BigDecimal.valueOf(hasPassList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 2, RoundingMode.HALF_UP);
            passRate.setCount(getTurnCount(divide));
        } else {
            passRate.setCount(0);
        }
        passList.add(passRate);
        NameCountDTO hasPass = new NameCountDTO();
        hasPass.setName(Translator.get("functional_case.hasPass"));
        hasPass.setCount(hasPassList.size());
        passList.add(hasPass);
        NameCountDTO unPass = new NameCountDTO();
        unPass.setName(Translator.get("functional_case.unPass"));
        unPass.setCount(statisticListByProjectId.size() - hasPassList.size());
        passList.add(unPass);
        return passList;
    }

    @NotNull
    private static List<NameCountDTO> getReviewList(Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap, List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<NameCountDTO> reviewList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> unReviewList = reviewStatusMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        if (CollectionUtils.isEmpty(unReviewList)) {
            unReviewList = new ArrayList<>();
        }
        NameCountDTO reviewRate = new NameCountDTO();
        reviewRate.setName(Translator.get("functional_case.reviewRate"));
        if (CollectionUtils.isEmpty(statisticListByProjectId)) {
            reviewRate.setCount(0);
        } else {
            BigDecimal divide = BigDecimal.valueOf(statisticListByProjectId.size() - unReviewList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 2, RoundingMode.HALF_UP);
            reviewRate.setCount(getTurnCount(divide));
        }
        reviewList.add(reviewRate);
        NameCountDTO hasReview = new NameCountDTO();
        hasReview.setName(Translator.get("functional_case.hasReview"));
        hasReview.setCount(statisticListByProjectId.size() - unReviewList.size());
        reviewList.add(hasReview);
        NameCountDTO unReview = new NameCountDTO();
        unReview.setName(Translator.get("functional_case.unReview"));
        unReview.setCount(unReviewList.size());
        reviewList.add(unReview);
        return reviewList;
    }

    @NotNull
    private static Integer getTurnCount(BigDecimal divide) {
        String value = String.valueOf(divide.multiply(BigDecimal.valueOf(100)));
        int i = value.indexOf(".");
        if (i > 0) {
            value = value.substring(0, i);
        }
        return Integer.valueOf(value);
    }

    private static void buildStatusPercentList(List<FunctionalCaseStatisticDTO> statisticListByProjectId, List<StatusPercentDTO> statusPercentList) {
        Map<String, List<FunctionalCaseStatisticDTO>> priorityMap = statisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getPriority));
        for (int i = 0; i < 4; i++) {
            String priority = "P" + i;
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(priority);
            List<FunctionalCaseStatisticDTO> functionalCaseStatisticDTOS = priorityMap.get(priority);
            if (CollectionUtils.isNotEmpty(functionalCaseStatisticDTOS)) {
                int size = functionalCaseStatisticDTOS.size();
                statusPercentDTO.setCount(size);
                BigDecimal divide = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
            } else {
                statusPercentDTO.setCount(0);
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        }
    }

    public StatisticsDTO projectAssociateCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        long caseTestCount = extFunctionalCaseMapper.caseTestCount(projectId, null, null);
        long simpleCaseCount = extFunctionalCaseMapper.simpleCaseCount(projectId, null, null);
        List<NameCountDTO> coverList = getCoverList((int) simpleCaseCount, (int) caseTestCount, (int) (simpleCaseCount - caseTestCount));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    public OverViewCountDTO projectBugHandleUser(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, BUG_MODULE, userId, PermissionConstants.PROJECT_BUG_READ)))
            return new OverViewCountDTO(null, new ArrayList<>(), new ArrayList<>(), NO_PROJECT_PERMISSION.getCode());
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<SelectOption> headerHandlerOption = getHandlerOption(request.getHandleUsers(), projectId);
        //获取每个人每个状态有多少数据(已按照用户id排序)
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(projectId);
        Set<String> platforms = getPlatforms(projectId);
        List<String> handleUserIds = headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getValue).collect(Collectors.toList());
        List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS = extBugMapper.projectUserBugStatusCount(projectId, toStartTime, toEndTime, handleUserIds, platforms);
        Map<String, SelectOption> statusMap = headerStatusOption.stream().collect(Collectors.toMap(SelectOption::getValue, t -> t));
        List<String> xaxis = new ArrayList<>(headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getText).toList());
        if (CollectionUtils.isEmpty(xaxis)) {
            xaxis.add(userId);
        }
        Map<String, List<Integer>> statusCountArrayMap = getStatusCountArrayMap(projectUserStatusCountDTOS, statusMap, handleUserIds);
        return getHandleUserCount(xaxis, statusMap, statusCountArrayMap);
    }

    @NotNull
    private static OverViewCountDTO getHandleUserCount(List<String> xaxis, Map<String, SelectOption> statusMap, Map<String, List<Integer>> statusCountArrayMap) {
        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        //组装X轴数据
        overViewCountDTO.setXAxis(xaxis);
        List<NameArrayDTO> projectCountList = getProjectCountList(statusMap, statusCountArrayMap);
        overViewCountDTO.setProjectCountList(projectCountList);
        return overViewCountDTO;
    }

    /**
     * 同一状态不同人的数量统计Map转换数据结构增加状态名称
     *
     * @param statusMap           状态名称集合
     * @param statusCountArrayMap 同一状态不同人的数量统计Map
     * @return List<NameArrayDTO>
     */
    private static List<NameArrayDTO> getProjectCountList(Map<String, SelectOption> statusMap, Map<String, List<Integer>> statusCountArrayMap) {
        List<NameArrayDTO> projectCountList = new ArrayList<>();
        statusCountArrayMap.forEach((status, countArray) -> {
            NameArrayDTO nameArrayDTO = new NameArrayDTO();
            SelectOption selectOption = statusMap.get(status);
            if (selectOption != null) {
                nameArrayDTO.setName(selectOption.getText());
            } else {
                nameArrayDTO.setName(status);
            }
            nameArrayDTO.setCount(countArray);
            projectCountList.add(nameArrayDTO);
        });
        return projectCountList;
    }

    /**
     * 根据处理人排序的处理人状态统计 将同一状态不同人的数量统计到一起
     *
     * @param projectUserStatusCountDTOS 根据处理人排序的处理人状态统计集合
     * @return 同一状态不同人的数量统计Map
     */
    private static Map<String, List<Integer>> getStatusCountArrayMap(List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS, Map<String, SelectOption> statusMap, List<String> handleUserIds) {
        Map<String, List<Integer>> statusCountArrayMap = new HashMap<>();
        Map<String, List<String>> statusUserArrayMap = new HashMap<>();
        for (ProjectUserStatusCountDTO projectUserStatusCountDTO : projectUserStatusCountDTOS) {
            String status = projectUserStatusCountDTO.getStatus();
            List<Integer> countList = statusCountArrayMap.get(status);
            List<String> userIds = statusUserArrayMap.get(status);
            if (CollectionUtils.isEmpty(countList)) {
                List<Integer> countArray = new ArrayList<>();
                List<String> userArray = new ArrayList<>();
                countArray.add(projectUserStatusCountDTO.getCount());
                userArray.add(projectUserStatusCountDTO.getUserId());
                statusCountArrayMap.put(status, countArray);
                statusUserArrayMap.put(status, userArray);
            } else {
                userIds.add(projectUserStatusCountDTO.getUserId());
                countList.add(projectUserStatusCountDTO.getCount());
                statusCountArrayMap.put(status, countList);
                statusUserArrayMap.put(status, userIds);
            }
        }
        List<Integer> countArray = new ArrayList<>();
        for (int i = 0; i < handleUserIds.size(); i++) {
            countArray.add(0);
        }
        statusMap.forEach((k, v) -> {
            List<Integer> handleUserCounts = statusCountArrayMap.get(k);
            List<String> userIds = statusUserArrayMap.get(k);
            if (CollectionUtils.isEmpty(handleUserCounts)) {
                statusCountArrayMap.put(k, countArray);
            } else {
                for (int i = 0; i < handleUserIds.size(); i++) {
                    if (userIds.size() > i) {
                        if (!StringUtils.equalsIgnoreCase(userIds.get(i), handleUserIds.get(i))) {
                            userIds.add(i, handleUserIds.get(i));
                            handleUserCounts.add(i, 0);
                        }
                    } else {
                        handleUserCounts.add(0);
                    }
                }
            }
        });
        return statusCountArrayMap;
    }

    /**
     * 获取当前项目根据筛选有多少处理人
     *
     * @param handleUsers 页面选择的处理人
     * @param projectId   项目id
     * @return 处理人id 与 名称的集合
     */
    private List<SelectOption> getHandlerOption(List<String> handleUsers, String projectId) {
        List<SelectOption> headerHandlerOption;
        if (CollectionUtils.isEmpty(handleUsers)) {
            headerHandlerOption = bugCommonService.getHeaderHandlerOption(projectId);
        } else {
            List<SelectOption> headerHandlerOptionList = bugCommonService.getHeaderHandlerOption(projectId);
            headerHandlerOption = headerHandlerOptionList.stream().filter(t -> handleUsers.contains(t.getValue())).toList();
        }
        return headerHandlerOption;
    }

    /**
     * 根据项目id获取当前对接平台，与本地进行组装
     *
     * @param projectId 项目ID
     * @return 本地与对接平台集合
     */
    private Set<String> getPlatforms(String projectId) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        Set<String> platforms = new HashSet<>();
        platforms.add(BugPlatform.LOCAL.getName());
        if (!StringUtils.equalsIgnoreCase(platformName, BugPlatform.LOCAL.getName())) {
            platforms.add(platformName);
        }
        return platforms;
    }

    public StatisticsDTO projectReviewCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<FunctionalCaseStatisticDTO> statisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, null, null);
        List<FunctionalCaseStatisticDTO> unReviewCaseList = statisticListByProjectId.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getReviewStatus(), FunctionalCaseReviewStatus.UN_REVIEWED.toString())).toList();
        int reviewCount = statisticListByProjectId.size() - unReviewCaseList.size();
        List<NameCountDTO> coverList = getCoverList(statisticListByProjectId.size(), reviewCount, unReviewCaseList.size());
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        List<StatusPercentDTO> statusPercentList = getStatusPercentList(statisticListByProjectId);
        statisticsDTO.setStatusPercentList(statusPercentList);
        return statisticsDTO;
    }

    public StatisticsDTO projectApiCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_DEFINITION_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<ApiDefinition> createAllApiList = extApiDefinitionMapper.getCreateApiList(projectId, null, null);
        Map<String, List<ApiDefinition>> protocolMap = createAllApiList.stream().collect(Collectors.groupingBy(ApiDefinition::getProtocol));
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<ProtocolDTO> protocols = apiTestService.getProtocols(request.getOrganizationId());
        int totalCount = CollectionUtils.isEmpty(createAllApiList) ? 0 : createAllApiList.size();
        for (ProtocolDTO protocol : protocols) {
            String protocolName = protocol.getProtocol();
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(protocolName);
            List<ApiDefinition> apiDefinitionList = protocolMap.get(protocolName);
            int size = CollectionUtils.isEmpty(apiDefinitionList) ? 0 : apiDefinitionList.size();
            if (totalCount == 0) {
                statusPercentDTO.setCount(0);
                statusPercentDTO.setPercentValue("0%");
            } else {
                statusPercentDTO.setCount(size);
                BigDecimal divide = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
            }
            statusPercentList.add(statusPercentDTO);
        }

        Map<String, List<ApiDefinition>> statusMap = createAllApiList.stream().collect(Collectors.groupingBy(ApiDefinition::getStatus));
        List<ApiDefinition> doneList = statusMap.get(ApiDefinitionStatus.DONE.toString());
        List<ApiDefinition> processList = statusMap.get(ApiDefinitionStatus.PROCESSING.toString());
        List<ApiDefinition> deprecatedList = statusMap.get(ApiDefinitionStatus.DEPRECATED.toString());
        List<ApiDefinition> debugList = statusMap.get(ApiDefinitionStatus.DEBUGGING.toString());
        List<NameCountDTO> nameCountDTOS = new ArrayList<>();
        NameCountDTO doneDTO = new NameCountDTO();
        doneDTO.setName(Translator.get("api_definition.status.completed"));
        NameCountDTO completionRate = new NameCountDTO();
        completionRate.setName(Translator.get("api_definition.completionRate"));

        int doneSize = CollectionUtils.isEmpty(doneList) ? 0 : doneList.size();
        if (totalCount == 0) {
            completionRate.setCount(0);
            doneDTO.setCount(0);
        } else {
            doneDTO.setCount(doneSize);
            BigDecimal divide = BigDecimal.valueOf(doneSize).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            completionRate.setCount(getTurnCount(divide));
        }
        NameCountDTO processDTO = getNameCountDTO(CollectionUtils.isEmpty(processList) ? 0 : processList.size(), Translator.get("api_definition.status.ongoing"));
        NameCountDTO deprecateDTO = getNameCountDTO(CollectionUtils.isEmpty(deprecatedList) ? 0 : deprecatedList.size(), Translator.get("api_definition.status.abandoned"));
        NameCountDTO debugDTO = getNameCountDTO(CollectionUtils.isEmpty(debugList) ? 0 : debugList.size(), Translator.get("api_definition.status.continuous"));
        nameCountDTOS.add(completionRate);
        nameCountDTOS.add(doneDTO);
        nameCountDTOS.add(processDTO);
        nameCountDTOS.add(debugDTO);
        nameCountDTOS.add(deprecateDTO);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("completionRate", nameCountDTOS);
        statisticsDTO.setStatusPercentList(statusPercentList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    @NotNull
    private static NameCountDTO getNameCountDTO(int size, String name) {
        NameCountDTO processDTO = new NameCountDTO();
        processDTO.setName(name);
        processDTO.setCount(size);
        return processDTO;
    }

    @NotNull
    private List<StatusPercentDTO> getStatusPercentList(List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<OptionDTO>statusNameList = buildStatusNameMap();
        int totalCount = CollectionUtils.isEmpty(statisticListByProjectId) ? 0 : statisticListByProjectId.size();
        Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap = statisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getReviewStatus));
        statusNameList.forEach(t->{
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            List<FunctionalCaseStatisticDTO> functionalCaseStatisticDTOS = reviewStatusMap.get(t.getId());
            int count = CollectionUtils.isEmpty(functionalCaseStatisticDTOS) ? 0 : functionalCaseStatisticDTOS.size();
            statusPercentDTO.setStatus(t.getName());
            statusPercentDTO.setCount(count);
            if (totalCount > 0) {
                BigDecimal divide = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
            } else {
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        });
        return statusPercentList;
    }

    @NotNull
    private static List<NameCountDTO> getCoverList(int totalCount, int coverCount, int unCoverCount) {
        List<NameCountDTO> coverList = new ArrayList<>();
        NameCountDTO coverRate = new NameCountDTO();
        if (totalCount > 0) {
            BigDecimal divide = BigDecimal.valueOf(coverCount).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            coverRate.setCount(getTurnCount(divide));
        }
        coverRate.setName(Translator.get("functional_case.coverRate"));
        coverList.add(coverRate);
        NameCountDTO hasCover = new NameCountDTO();
        hasCover.setCount(coverCount);
        hasCover.setName(Translator.get("functional_case.hasCover"));
        coverList.add(hasCover);
        NameCountDTO unCover = new NameCountDTO();
        unCover.setCount(unCoverCount);
        unCover.setName(Translator.get("functional_case.unCover"));
        coverList.add(unCover);
        return coverList;
    }

    private static List<OptionDTO> buildStatusNameMap() {
        List<OptionDTO>optionDTOList = new ArrayList<>();
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UN_REVIEWED.toString(), Translator.get("case.review.status.un_reviewed")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString(), Translator.get("case.review.status.under_reviewed")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.PASS.toString(), Translator.get("case.review.status.pass")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UN_PASS.toString(), Translator.get("case.review.status.un_pass")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.RE_REVIEWED.toString(), Translator.get("case.review.status.re_reviewed")));
        return optionDTOList;
    }

    public Pager<List<CaseReviewDTO>> getFunctionalCasePage(DashboardFrontPageRequest request) {
        CaseReviewPageRequest reviewRequest = getCaseReviewPageRequest(request);
        Page<Object> page = PageHelper.startPage(reviewRequest.getCurrent(), reviewRequest.getPageSize(),
                StringUtils.isNotBlank(reviewRequest.getSortString()) ? reviewRequest.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(reviewRequest));
    }


    @NotNull
    private static CaseReviewPageRequest getCaseReviewPageRequest(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        CaseReviewPageRequest reviewRequest = new CaseReviewPageRequest();
        reviewRequest.setProjectId(projectId);
        reviewRequest.setPageSize(request.getPageSize());
        reviewRequest.setCurrent(request.getCurrent());
        reviewRequest.setSort(request.getSort());
        CombineSearch combineSearch = getCombineSearch(request);
        reviewRequest.setCombineSearch(combineSearch);
        return reviewRequest;
    }

    @NotNull
    private static CombineSearch getCombineSearch(DashboardFrontPageRequest request) {
        CombineSearch combineSearch = new CombineSearch();
        combineSearch.setSearchMode(CombineSearch.SearchMode.AND.name());
        List<CombineCondition> conditions = new ArrayList<>();
        CombineCondition userCombineCondition = getCombineCondition(List.of(Objects.requireNonNull(SessionUtils.getUserId())), "reviewers", CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(userCombineCondition);
        CombineCondition statusCombineCondition = getCombineCondition(List.of(CaseReviewStatus.PREPARED.toString(), CaseReviewStatus.UNDERWAY.toString()), "status", CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(statusCombineCondition);
        CombineCondition createTimeCombineCondition = getCombineCondition(List.of(request.getToStartTime(), request.getToEndTime()), "createTime", CombineCondition.CombineConditionOperator.BETWEEN.toString());
        conditions.add(createTimeCombineCondition);
        combineSearch.setConditions(conditions);
        return combineSearch;
    }

    @NotNull
    private static CombineCondition getCombineCondition(List<Object> value, String reviewers, String operator) {
        CombineCondition userCombineCondition = new CombineCondition();
        userCombineCondition.setValue(value);
        userCombineCondition.setName(reviewers);
        userCombineCondition.setOperator(operator);
        userCombineCondition.setCustomField(false);
        userCombineCondition.setCustomFieldType("");
        return userCombineCondition;
    }

    public List<ApiDefinitionUpdateDTO> getApiUpdatePage(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<ApiDefinitionUpdateDTO> list = extApiDefinitionMapper.getUpdateApiList(projectId, toStartTime, toEndTime);
        processApiDefinitions(projectId, list);
        return list;
    }

    private void processApiDefinitions(String projectId, List<ApiDefinitionUpdateDTO> list) {
        List<String> apiDefinitionIds = list.stream().map(ApiDefinitionUpdateDTO::getId).toList();
        if (CollectionUtils.isEmpty(apiDefinitionIds)) {
            return;
        }
        List<ApiTestCase> apiCaseList = extApiDefinitionMapper.selectNotInTrashCaseIdsByApiIds(apiDefinitionIds);
        Map<String, List<ApiTestCase>> apiCaseMap = apiCaseList.stream().
                collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        List<ApiRefSourceCountDTO> apiRefSourceCountDTOS = extApiDefinitionMapper.scenarioRefApiCount(projectId, apiDefinitionIds);
        Map<String, Integer> countMap = apiRefSourceCountDTOS.stream().collect(Collectors.toMap(ApiRefSourceCountDTO::getSourceId, ApiRefSourceCountDTO::getCount));
        list.forEach(item -> {
            // Calculate API Case Metrics
            List<ApiTestCase> apiTestCases = apiCaseMap.get(item.getId());
            if (apiTestCases != null) {
                item.setCaseTotal(apiTestCases.size());
            } else {
                item.setCaseTotal(0);
            }
            Integer count = countMap.get(item.getId());
            item.setScenarioTotal(Objects.requireNonNullElse(count, 0));
        });
    }

    public List<SelectOption> getBugHandleUserList(String projectId) {
        return getHandlerOption(null, projectId);
    }

    public StatisticsDTO projectApiCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_DEFINITION_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        long unDeleteCaseExecCount = extExecTaskItemMapper.getUnDeleteCaseExecCount(projectId, null, null, List.of("PLAN_RUN_API_CASE", "API_CASE"));
        List<ApiTestCase> simpleAllApiCaseList = extApiTestCaseMapper.getSimpleApiCaseList(projectId, null, null);

        int simpleAllApiCaseSize = 0;
        if (CollectionUtils.isNotEmpty(simpleAllApiCaseList)) {
            simpleAllApiCaseSize = simpleAllApiCaseList.size();
        }
        List<ApiTestCase> unExecList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ExecStatus.PENDING.toString())).toList();
        int unExecSize = CollectionUtils.isNotEmpty(unExecList) ? unExecList.size() : 0;

        List<ApiTestCase> successList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.SUCCESS.name())).toList();
        int successSize = CollectionUtils.isNotEmpty(successList) ? successList.size() : 0;
        List<ApiTestCase> errorList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.ERROR.name())).toList();
        int errorSize = CollectionUtils.isNotEmpty(errorList) ? errorList.size() : 0;

        List<ApiTestCase> fakeList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.FAKE_ERROR.name())).toList();
        int fakeSize = CollectionUtils.isNotEmpty(fakeList) ? fakeList.size() : 0;

        List<NameCountDTO> execDTOS = getExecDTOS((int) unDeleteCaseExecCount);
        statusStatisticsMap.put("execCount", execDTOS);
        List<NameCountDTO> execRateDTOS = getExecRateDTOS(unExecSize, simpleAllApiCaseSize, Translator.get("api_management.apiCaseExecRate"));
        statusStatisticsMap.put("execRate", execRateDTOS);
        List<NameCountDTO> passRateDTOS = getPassRateDTOS(successSize, errorSize, simpleAllApiCaseSize, Translator.get("api_management.apiCasePassRate"));
        statusStatisticsMap.put("passRate", passRateDTOS);
        List<NameCountDTO> apiCaseDTOS = getApiCaseDTOS(fakeSize, simpleAllApiCaseSize, Translator.get("api_management.apiCaseCount"));
        statusStatisticsMap.put("apiCaseCount", apiCaseDTOS);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    @NotNull
    private List<NameCountDTO> getApiCaseDTOS(int fakeSize, int simpleApiCaseSize, String name) {
        List<NameCountDTO> apiCaseDTOS = new ArrayList<>();
        NameCountDTO apiCaseCountDTO = getNameCountDTO(simpleApiCaseSize, name);
        apiCaseDTOS.add(apiCaseCountDTO);
        NameCountDTO fakeCountDTO = getNameCountDTO(fakeSize, Translator.get("api_management.fakeErrorCount"));
        apiCaseDTOS.add(fakeCountDTO);
        return apiCaseDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getPassRateDTOS(int successSize, int errorSize, int simpleAllApiCaseSize, String name) {
        List<NameCountDTO> passRateDTOS = new ArrayList<>();
        NameCountDTO passRateDTO = new NameCountDTO();
        passRateDTO.setName(name);
        if (simpleAllApiCaseSize == 0) {
            passRateDTO.setCount(0);
        } else {
            BigDecimal divide = BigDecimal.valueOf(successSize).divide(BigDecimal.valueOf(simpleAllApiCaseSize), 2, RoundingMode.HALF_UP);
            passRateDTO.setCount(getTurnCount(divide));
        }
        passRateDTOS.add(passRateDTO);
        NameCountDTO unPassDTO = getNameCountDTO(errorSize, Translator.get("api_management.unPassCount"));
        passRateDTOS.add(unPassDTO);
        NameCountDTO passDTO = getNameCountDTO(successSize, Translator.get("api_management.passCount"));
        passRateDTOS.add(passDTO);
        return passRateDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getExecRateDTOS(int unExecSize, int simpleAllApiCaseSize, String name) {
        List<NameCountDTO> execRateDTOS = new ArrayList<>();
        NameCountDTO execRateDTO = new NameCountDTO();
        execRateDTO.setName(name);
        if (simpleAllApiCaseSize == 0) {
            execRateDTO.setCount(0);
        } else {
            BigDecimal divide = BigDecimal.valueOf(simpleAllApiCaseSize - unExecSize).divide(BigDecimal.valueOf(simpleAllApiCaseSize), 2, RoundingMode.HALF_UP);
            execRateDTO.setCount(getTurnCount(divide));
        }
        execRateDTOS.add(execRateDTO);
        NameCountDTO unExecDTO = getNameCountDTO(unExecSize, Translator.get("api_management.unExecCount"));
        execRateDTOS.add(unExecDTO);
        NameCountDTO execDTO = getNameCountDTO(simpleAllApiCaseSize - unExecSize, Translator.get("api_management.execCount"));
        execRateDTOS.add(execDTO);
        return execRateDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getExecDTOS(int unDeleteCaseExecCount) {
        List<NameCountDTO> execDTOS = new ArrayList<>();
        NameCountDTO execCountDTO = new NameCountDTO();
        execCountDTO.setName(Translator.get("api_management.execTime"));
        execCountDTO.setCount(unDeleteCaseExecCount);
        execDTOS.add(execCountDTO);
        return execDTOS;
    }

    public StatisticsDTO projectApiScenarioCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_SCENARIO_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }

        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        long unDeleteCaseExecCount = extExecTaskItemMapper.getUnDeleteScenarioExecCount(projectId, null, null, List.of("PLAN_RUN_API_SCENARIO", "API_SCENARIO"));
        List<ApiScenario> simpleAllApiScenarioList = extApiScenarioMapper.getSimpleApiScenarioList(projectId, null, null);

        int simpleAllApiScenarioSize = 0;
        if (CollectionUtils.isNotEmpty(simpleAllApiScenarioList)) {
            simpleAllApiScenarioSize = simpleAllApiScenarioList.size();
        }
        List<ApiScenario> unExecList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ExecStatus.PENDING.toString())).toList();
        int unExecSize = CollectionUtils.isNotEmpty(unExecList) ? unExecList.size() : 0;

        List<ApiScenario> successList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.SUCCESS.name())).toList();
        int successSize = CollectionUtils.isNotEmpty(successList) ? successList.size() : 0;
        List<ApiScenario> errorList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.ERROR.name())).toList();
        int errorSize = CollectionUtils.isNotEmpty(errorList) ? errorList.size() : 0;

        List<ApiScenario> fakeList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.FAKE_ERROR.name())).toList();
        int fakeSize = CollectionUtils.isNotEmpty(fakeList) ? fakeList.size() : 0;

        List<NameCountDTO> execDTOS = getExecDTOS((int) unDeleteCaseExecCount);
        statusStatisticsMap.put("execCount", execDTOS);
        List<NameCountDTO> execRateDTOS = getExecRateDTOS(unExecSize, simpleAllApiScenarioSize, Translator.get("api_management.scenarioExecRate"));
        statusStatisticsMap.put("execRate", execRateDTOS);
        List<NameCountDTO> passRateDTOS = getPassRateDTOS(successSize, errorSize, simpleAllApiScenarioSize, Translator.get("api_management.scenarioPassRate"));
        statusStatisticsMap.put("passRate", passRateDTOS);
        List<NameCountDTO> apiCaseDTOS = getApiCaseDTOS(fakeSize, simpleAllApiScenarioSize, Translator.get("api_management.apiScenarioCount"));
        statusStatisticsMap.put("apiScenarioCount", apiCaseDTOS);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    public StatisticsDTO baseProjectBugCount(DashboardFrontPageRequest request, String userId, String handleUserId, Boolean hasHandleUser, Boolean hasCreateUser) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, BUG_MODULE, userId, PermissionConstants.PROJECT_BUG_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        String localHandleUser = hasHandleUser ? userId : null;
        String handleUser = hasHandleUser ? handleUserId : null;
        String createUser = hasCreateUser ? userId : null;
        Set<String> platforms = getPlatforms(projectId);
        List<Bug> allSimpleList = extBugMapper.getSimpleList(projectId, null, null, handleUser, createUser, platforms, localHandleUser);
        List<String> localLastStepStatus = getBugEndStatus(projectId);
        List<Bug> statusList = allSimpleList.stream().filter(t -> !localLastStepStatus.contains(t.getStatus())).toList();
        int statusSize = CollectionUtils.isEmpty(statusList) ? 0 : statusList.size();
        int totalSize = CollectionUtils.isEmpty(allSimpleList) ? 0 : allSimpleList.size();
        List<NameCountDTO> nameCountDTOS = buildBugRetentionRateList(totalSize, statusSize);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("retentionRate", nameCountDTOS);
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(projectId);
        Map<String, List<Bug>> bugMap = allSimpleList.stream().collect(Collectors.groupingBy(Bug::getStatus));
        List<StatusPercentDTO> bugPercentList = bulidBugPercentList(headerStatusOption, bugMap, totalSize);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        statisticsDTO.setStatusPercentList(bugPercentList);
        return statisticsDTO;
    }

    @NotNull
    private List<String> getBugEndStatus(String projectId) {
        List<String> localLastStepStatus = bugCommonService.getLocalLastStepStatus(projectId);
        String platformName = projectApplicationService.getPlatformName(projectId);
        if (StringUtils.equalsIgnoreCase(platformName, BugPlatform.LOCAL.getName())) {
            return localLastStepStatus;
        }
        // 项目对接三方平台
        List<String> platformLastStepStatus = new ArrayList<>();
        try {
            platformLastStepStatus = bugCommonService.getPlatformLastStepStatus(projectId);
        } catch (Exception e) {
            // 获取三方平台结束状态失败, 只过滤本地结束状态
            LogUtils.error(Translator.get("get_platform_end_status_error"));
        }
        localLastStepStatus.addAll(platformLastStepStatus);
        return localLastStepStatus;
    }


    private static List<NameCountDTO> buildBugRetentionRateList(int totalSize, int statusSize) {
        List<NameCountDTO> retentionRates = new ArrayList<>();
        NameCountDTO retentionRate = new NameCountDTO();
        retentionRate.setName(Translator.get("bug_management.retentionRate"));
        if (totalSize == 0) {
            retentionRate.setCount(0);
        } else {
            BigDecimal divide = BigDecimal.valueOf(statusSize).divide(BigDecimal.valueOf(totalSize), 2, RoundingMode.HALF_UP);
            retentionRate.setCount(getTurnCount(divide));
        }
        retentionRates.add(retentionRate);
        NameCountDTO total = getNameCountDTO(totalSize, Translator.get("bug_management.totalCount"));
        retentionRates.add(total);
        NameCountDTO retentionDTO = getNameCountDTO(statusSize, Translator.get("bug_management.retentionCount"));
        retentionRates.add(retentionDTO);
        return retentionRates;
    }

    private static List<StatusPercentDTO> bulidBugPercentList(List<SelectOption> headerStatusOption, Map<String, List<Bug>> bugMap, int simpleSize) {
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        for (SelectOption selectOption : headerStatusOption) {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(selectOption.getText());
            List<Bug> bugs = bugMap.get(selectOption.getValue());
            int bugSize = CollectionUtils.isEmpty(bugs) ? 0 : bugs.size();
            if (simpleSize == 0) {
                statusPercentDTO.setPercentValue("0%");
                statusPercentDTO.setCount(0);
            } else {
                BigDecimal divide = BigDecimal.valueOf(bugSize).divide(BigDecimal.valueOf(simpleSize), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
                statusPercentDTO.setCount(bugSize);
            }
            statusPercentList.add(statusPercentDTO);
        }
        return statusPercentList;
    }

    public StatisticsDTO projectBugCount(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, false, false);
    }

    public StatisticsDTO projectBugCountCreateByMe(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, false, true);
    }

    public StatisticsDTO projectBugCountHandleByMe(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, true, false);
    }

    public StatisticsDTO projectPlanLegacyBug(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, TEST_PLAN_MODULE, userId, PermissionConstants.TEST_PLAN_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        Set<String> platforms = getPlatforms(projectId);
        List<SelectOption> planBugList = extTestPlanMapper.getPlanBugList(projectId, TestPlanConstants.TEST_PLAN_TYPE_PLAN, new ArrayList<>(platforms), null);
        List<String> localLastStepStatus = getBugEndStatus(projectId);
        List<SelectOption> legacyBugList = planBugList.stream().filter(t -> !localLastStepStatus.contains(t.getText())).toList();
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(projectId);
        int statusSize = CollectionUtils.isEmpty(legacyBugList) ? 0 : legacyBugList.size();
        int totalSize = CollectionUtils.isEmpty(planBugList) ? 0 : planBugList.size();
        List<NameCountDTO> nameCountDTOS = buildBugRetentionRateList(totalSize, statusSize);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("retentionRate", nameCountDTOS);
        Map<String, List<SelectOption>> bugMap = legacyBugList.stream().collect(Collectors.groupingBy(SelectOption::getValue));
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        for (SelectOption selectOption : headerStatusOption) {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(selectOption.getText());
            List<SelectOption> bugs = bugMap.get(selectOption.getValue());
            int bugSize = CollectionUtils.isEmpty(bugs) ? 0 : bugs.size();
            if (statusSize == 0) {
                statusPercentDTO.setPercentValue("0%");
                statusPercentDTO.setCount(0);
            } else {
                BigDecimal divide = BigDecimal.valueOf(bugSize).divide(BigDecimal.valueOf(statusSize), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
                statusPercentDTO.setCount(bugSize);
            }
            statusPercentList.add(statusPercentDTO);
        }
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        statisticsDTO.setStatusPercentList(statusPercentList);
        return statisticsDTO;
    }
}


