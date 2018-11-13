package Com.VSummary.service;

import Com.VSummary.domain.*;
import Com.VSummary.repository.SummariesRepository;
import Com.VSummary.repository.SummariesSearchRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
public class MainService {
    @Autowired
    private SummariesRepository summariesRepository;
    @Autowired
    private SummariesSearchRepository summariesSearchRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public String writeSummary(Summaries summaries) {
        summariesRepository.save(summaries);
        summariesSearchRepository.save(new SummariesSearch(summaries));

        writeTags(summaries.getSumaryTags().split("\\s+"));

        return "{\"success\":true}";
    }

    private void writeTags(String[] tags) {
        List<IndexQuery> indexQueries = new ArrayList<>();

        Arrays.stream(tags).forEach(tag -> {
            indexQueries.add(new SummarySearchTagBuilder().name(tag).suggest(new String[]{tag}).buildIndex());
        });

        elasticsearchTemplate.bulkIndex(indexQueries);
        elasticsearchTemplate.refresh(SummarySearchTag.class);
    }

    public void sendTags(String tag, String username) {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders
                .completionSuggestion("suggest")
                .prefix(tag, Fuzziness.AUTO);

        final SearchResponse suggestResponse  = elasticsearchTemplate.suggest(
                new SuggestBuilder().addSuggestion(
                        "test-suggest",
                        completionSuggestionFuzzyBuilder),
                SummarySearchTag.class);

        if (suggestResponse.getSuggest() == null)
            return;

        CompletionSuggestion completionSuggestion = suggestResponse.getSuggest().getSuggestion("test-suggest");
        List<CompletionSuggestion.Entry.Option> options = completionSuggestion.getEntries().get(0).getOptions();

        List<SimpleMessage> messages = new ArrayList<>();

        for (int i = 0; (i < options.size()) && (i < 5); i++) {
            messages.add(new SimpleMessage(options.get(i).getText().string()));
        }

        simpMessagingTemplate.convertAndSendToUser(username, "queue/updateTags", messages);
    }

    public void sendSearchName(String search, String username) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(multiMatchQuery(search)
                        .field("name")
                        .field("shortDescription")
                        .field("specialtyNumber")
                        .field("text")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                        .fuzziness(Fuzziness.AUTO)
                        .prefixLength(3)
                )
                .build();

        Page<SummariesSearch> results = summariesSearchRepository.search(searchQuery);

        List<SimpleMessage> simpleMessages = new ArrayList<>();
        results.forEach(result -> simpleMessages.add(new SimpleMessage(result.getName())));

        simpMessagingTemplate.convertAndSendToUser(username, "queue/searchData", simpleMessages);
    }
}
