package jombi.freemates.util.docs;

import io.swagger.v3.oas.models.Operation;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
@RequiredArgsConstructor
public class CustomOperationCustomizer implements OperationCustomizer {

  private final GithubIssueService githubIssueService;

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    MergedAnnotations annotations = MergedAnnotations.from(handlerMethod.getMethod(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
    MergedAnnotation<ApiChangeLogs> apiChangeLogsAnnotation = annotations.get(ApiChangeLogs.class);

    if (apiChangeLogsAnnotation.isPresent()) {
      ApiChangeLog[] apiChangeLogs = apiChangeLogsAnnotation.synthesize().value();

      Set<Integer> issueNumbers = new TreeSet<>();
      for (ApiChangeLog log : apiChangeLogs) {
        if (log.issueNumber() > 0) {
          issueNumbers.add(log.issueNumber());
        }
      }

      StringBuilder tableBuilder = new StringBuilder();
      tableBuilder.append("\n\n**변경 관리 이력:**\n")
          .append("<table>")
          .append("<thead>")
          .append("<tr>")
          .append("<th>날짜</th>")
          .append("<th>작성자</th>")
          .append("<th>이슈번호</th>")
          .append("<th>이슈 제목</th>")
          .append("<th>변경 내용</th>")
          .append("</tr>")
          .append("</thead>")
          .append("<tbody>");

      for (ApiChangeLog log : apiChangeLogs) {
        String description = log.description()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
        String issueNumberCell = "";
        String issueTitleCell = "";

        if (log.issueNumber() > 0) {
          issueNumberCell = String.format("<a href=\"%s%d\" target=\"_blank\">#%d</a>",
              GithubIssueService.ISSUE_BASE_URL,
              log.issueNumber(),
              log.issueNumber());
          try {
            issueTitleCell = githubIssueService.getOrFetchIssue(log.issueNumber()).getCleanTitle();
            // 제목도 HTML 엔티티로 이스케이프
            issueTitleCell = issueTitleCell
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
          } catch (Exception e) {
            issueTitleCell = "ERROR";
          }
        }

        tableBuilder.append(String.format(
            "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
            log.date(),
            log.author().getDisplayName(),
            issueNumberCell,
            issueTitleCell,
            description));
      }

      tableBuilder.append("</tbody>")
          .append("</table>");

      String originalDescription = operation.getDescription() != null ? operation.getDescription() : "";
      operation.setDescription(originalDescription + tableBuilder.toString());
    }
    return operation;
  }
}