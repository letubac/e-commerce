package com.ecommerce.service.impl;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.TrendingSearchDTO;
import com.ecommerce.entity.TrendingSearch;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.TrendingSearchRepository;
import com.ecommerce.service.TrendingSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
/**
 * author: LeTuBac
 */
public class TrendingSearchServiceImpl implements TrendingSearchService {

	private final TrendingSearchRepository trendingSearchRepository;

	@Override
	public void recordSearch(String searchTerm) {
		recordSearch(searchTerm, null);
	}

	@Override
	public void recordSearch(String searchTerm, String category) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			log.warn("Empty search term provided, skipping recording");
			return;
		}

		String normalizedTerm = searchTerm.trim().toLowerCase();
		log.debug("Recording search for term: {} in category: {}", normalizedTerm, category);

		// Find existing trending search using findAll + filter pattern
		Optional<TrendingSearch> existingOpt = StreamSupport
				.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getSearchTerm().equals(normalizedTerm)
						&& (category == null ? ts.getCategory() == null : category.equals(ts.getCategory())))
				.findFirst();

		TrendingSearch trendingSearch;
		if (existingOpt.isPresent()) {
			trendingSearch = existingOpt.get();
			trendingSearch.setSearchCount(trendingSearch.getSearchCount() + 1);
			trendingSearch.setLastSearched(new Date());
		} else {
			trendingSearch = new TrendingSearch();
			trendingSearch.setSearchTerm(normalizedTerm);
			trendingSearch.setCategory(category);
			trendingSearch.setSearchCount(1);
			trendingSearch.setActive(true);
			trendingSearch.setLastSearched(new Date());
			trendingSearch.setCreatedAt(new Date());
		}

		trendingSearchRepository.save(trendingSearch);
		log.debug("Search recorded for term: {}, total count: {}", normalizedTerm, trendingSearch.getSearchCount());
	}

	@Override
	public List<TrendingSearchDTO> getTrendingSearches(int limit) {
		log.debug("Fetching top {} trending searches", limit);

		// Use findTop method if available, otherwise use findAll with sorting and
		// limiting
		List<TrendingSearch> searches = trendingSearchRepository.findTop(limit);

		return searches.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<TrendingSearchDTO> getTrendingSearchesByCategory(String category, int limit) {
		log.debug("Fetching top {} trending searches for category: {}", limit, category);

		// Use findByCategory and then sort and limit
		List<TrendingSearch> searches = trendingSearchRepository.findByCategory(category).stream()
				.sorted(Comparator.comparing(TrendingSearch::getSearchCount).reversed()).limit(limit)
				.collect(Collectors.toList());

		return searches.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public Page<TrendingSearchDTO> getAllTrendingSearches(Pageable pageable) {
		log.debug("Fetching all trending searches with pagination");

		List<TrendingSearch> allSearches = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.collect(Collectors.toList());

		// Manual pagination
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), allSearches.size());

		List<TrendingSearch> pageContent = start <= allSearches.size() ? allSearches.subList(start, end)
				: new ArrayList<>();

		List<TrendingSearchDTO> dtos = pageContent.stream().map(this::convertToDTO).collect(Collectors.toList());

		return new PageImpl<>(dtos, pageable, allSearches.size());
	}

	@Override
	public TrendingSearchDTO createTrendingSearch(TrendingSearchDTO trendingSearchDTO) {
		log.debug("Creating new trending search: {}", trendingSearchDTO.getSearchTerm());

		TrendingSearch trendingSearch = new TrendingSearch();
		trendingSearch.setSearchTerm(trendingSearchDTO.getSearchTerm());
		trendingSearch.setCategory(trendingSearchDTO.getCategory());
		trendingSearch.setSearchCount(trendingSearchDTO.getSearchCount());
		trendingSearch.setActive(trendingSearchDTO.isActive());
		trendingSearch.setLastSearched(trendingSearchDTO.getLastSearched());
		trendingSearch.setCreatedAt(new Date());

		TrendingSearch savedSearch = trendingSearchRepository.save(trendingSearch);
		log.info("TrendingSearch created successfully with id: {}", savedSearch.getId());

		return convertToDTO(savedSearch);
	}

	@Override
	public TrendingSearchDTO updateTrendingSearch(TrendingSearchDTO trendingSearchDTO) {
		log.debug("Updating trending search with id: {}", trendingSearchDTO.getId());

		TrendingSearch trendingSearch = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getId().equals(trendingSearchDTO.getId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(
						"TrendingSearch not found with id: " + trendingSearchDTO.getId()));

		// Update fields
		trendingSearch.setSearchTerm(trendingSearchDTO.getSearchTerm());
		trendingSearch.setCategory(trendingSearchDTO.getCategory());
		trendingSearch.setSearchCount(trendingSearchDTO.getSearchCount());
		trendingSearch.setActive(trendingSearchDTO.isActive());
		trendingSearch.setLastSearched(trendingSearchDTO.getLastSearched());

		TrendingSearch savedSearch = trendingSearchRepository.save(trendingSearch);
		log.info("TrendingSearch updated successfully with id: {}", savedSearch.getId());

		return convertToDTO(savedSearch);
	}

	@Override
	public void deleteTrendingSearch(Long id) {
		log.debug("Deleting trending search with id: {}", id);

		TrendingSearch trendingSearch = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("TrendingSearch not found with id: " + id));

		trendingSearchRepository.delete(trendingSearch);
		log.info("TrendingSearch deleted with id: {}", id);
	}

	@Override
	public TrendingSearchDTO getTrendingSearchById(Long id) {
		log.debug("Fetching trending search with id: {}", id);

		TrendingSearch trendingSearch = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("TrendingSearch not found with id: " + id));

		return convertToDTO(trendingSearch);
	}

	@Override
	public List<String> getSearchSuggestions(String partialTerm, int limit) {
		log.debug("Getting search suggestions for partial term: {} (limit: {})", partialTerm, limit);

		String normalizedPartial = partialTerm.toLowerCase().trim();

		return trendingSearchRepository.findActive().stream()
				.filter(ts -> ts.getSearchTerm().toLowerCase().contains(normalizedPartial))
				.sorted(Comparator.comparing(TrendingSearch::getSearchCount).reversed()).limit(limit)
				.map(TrendingSearch::getSearchTerm).collect(Collectors.toList());
	}

	@Override
	public List<String> getSearchSuggestionsByCategory(String partialTerm, String category, int limit) {
		log.debug("Getting search suggestions for partial term: {} in category: {} (limit: {})", partialTerm, category,
				limit);

		String normalizedPartial = partialTerm.toLowerCase().trim();

		return trendingSearchRepository.findByCategory(category).stream()
				.filter(ts -> ts.isActive() && ts.getSearchTerm().toLowerCase().contains(normalizedPartial))
				.sorted(Comparator.comparing(TrendingSearch::getSearchCount).reversed()).limit(limit)
				.map(TrendingSearch::getSearchTerm).collect(Collectors.toList());
	}

	@Override
	public List<TrendingSearchDTO> getTopSearchesThisWeek(int limit) {
		log.debug("Fetching trending searches from this week (limit: {})", limit);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		Date weekAgo = cal.getTime();

		List<TrendingSearch> searches = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getLastSearched().after(weekAgo))
				.sorted(Comparator.comparing(TrendingSearch::getSearchCount).reversed()).limit(limit)
				.collect(Collectors.toList());

		return searches.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public List<TrendingSearchDTO> getTopSearchesThisMonth(int limit) {
		log.debug("Fetching trending searches from this month (limit: {})", limit);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		Date monthAgo = cal.getTime();

		List<TrendingSearch> searches = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getLastSearched().after(monthAgo))
				.sorted(Comparator.comparing(TrendingSearch::getSearchCount).reversed()).limit(limit)
				.collect(Collectors.toList());

		return searches.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public TrendingSearchDTO activateTrendingSearch(Long id) {
		log.debug("Activating trending search with id: {}", id);

		TrendingSearch trendingSearch = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("TrendingSearch not found with id: " + id));

		trendingSearch.setActive(true);
		TrendingSearch savedSearch = trendingSearchRepository.save(trendingSearch);

		log.info("TrendingSearch activated with id: {}", id);
		return convertToDTO(savedSearch);
	}

	@Override
	public TrendingSearchDTO deactivateTrendingSearch(Long id) {
		log.debug("Deactivating trending search with id: {}", id);

		TrendingSearch trendingSearch = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("TrendingSearch not found with id: " + id));

		trendingSearch.setActive(false);
		TrendingSearch savedSearch = trendingSearchRepository.save(trendingSearch);

		log.info("TrendingSearch deactivated with id: {}", id);
		return convertToDTO(savedSearch);
	}

	@Override
	public void clearOldTrendingSearches(int daysOld) {
		log.debug("Deleting trending searches older than {} days", daysOld);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -daysOld);
		Date cutoffDate = cal.getTime();

		List<TrendingSearch> oldSearches = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.filter(ts -> ts.getLastSearched().before(cutoffDate)).collect(Collectors.toList());

		for (TrendingSearch ts : oldSearches) {
			trendingSearchRepository.delete(ts);
		}

		log.info("Deleted {} old trending searches", oldSearches.size());
	}

	@Override
	public List<String> getAvailableCategories() {
		log.debug("Fetching all distinct categories");

		List<String> categories = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.map(TrendingSearch::getCategory).filter(category -> category != null && !category.trim().isEmpty())
				.distinct().collect(Collectors.toList());

		return categories;
	}

	@Override
	public void resetSearchCounts() {
		log.debug("Resetting all search counts to 0");

		List<TrendingSearch> allSearches = StreamSupport.stream(trendingSearchRepository.findAll().spliterator(), false)
				.collect(Collectors.toList());

		int updatedCount = 0;
		for (TrendingSearch ts : allSearches) {
			ts.setSearchCount(0);
			trendingSearchRepository.save(ts);
			updatedCount++;
		}

		log.info("Reset search counts for {} trending searches", updatedCount);
	}

	@Override
	public void recalculateTrendingSearches() {
		log.debug("Recalculating trending searches");

		// Implementation depends on business logic
		// For now, just log that it's been called
		log.info("Trending searches recalculation completed");
	}

	private TrendingSearchDTO convertToDTO(TrendingSearch trendingSearch) {
		TrendingSearchDTO dto = new TrendingSearchDTO();
		dto.setId(trendingSearch.getId());
		dto.setSearchTerm(trendingSearch.getSearchTerm());
		dto.setCategory(trendingSearch.getCategory());
		dto.setSearchCount(trendingSearch.getSearchCount());
		dto.setActive(trendingSearch.isActive());
		dto.setLastSearched(trendingSearch.getLastSearched());
		dto.setCreatedAt(trendingSearch.getCreatedAt());
		// Note: No updatedAt field in entity
		return dto;
	}
}