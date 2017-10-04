package com.sysgears.theme.pagination

class Paginator {

    /**
     * Creates pages for pagination
     *
     * @param pages source where to search pages for pagination.
     * @param nameInModel name of variable to write result pages (e.g. posts will be available in view as page.posts)
     * @param perPage how much pages to grab for a single page
     * @param defaultUrl url, on which, urls of all the pages of this pagination will be based
     * @param customModel append custom model for each pagination page
     *
     * @return list of pagination models
     */
    static List paginate(pages, nameInModel, perPage, defaultUrl, customModel) {
        def pageUrl = { pageNo -> defaultUrl + (pageNo > 1 ? "page/$pageNo/" : '') }
        def splitOnPages = pages.collate(perPage)
        def numPages = splitOnPages.size()
        def pageNo = 0
        splitOnPages.collect { itemsOnPage ->
            def model = [url: (pageUrl(++pageNo)), (nameInModel): itemsOnPage, paginator: [:]]
            if (pageNo > 1) {
                model.paginator.prev_page = pageUrl(pageNo - 1)
            }
            if (pageNo < numPages) {
                model.paginator.next_page = pageUrl(pageNo + 1)
            }
            customModel + model
        }
    }
}
