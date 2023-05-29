document.addEventListener('DOMContentLoaded', async () => {
    const pages = await Promise.all((() => {
        return [
            {id: "home", displayName: "Home", div: document.getElementById("home-page")},
            {id: "buy", displayName: "Buy", div: document.getElementById("buy-page"), view: buyPage},
            {id: "sell", displayName: "Sell", div: document.getElementById("sell-page"), view: sellPage},
            {id: "auction-details", div: document.getElementById("auction-details-page"), view: auctionDetailsPage},
        ].map(async d => {
            const view = d.view ? new d.view(d.div) : undefined
            if (view)
                await view.create()
            d.view = view
            return d
        })
    })());

    const selectedPage = (() => {
        let selectedPage = pages[0];
        const obj = {
            get: () => {
                return selectedPage
            },
            set: async (newPage, args) => {
                selectedPage.div.setAttribute("hidden", "");
                if (selectedPage.view)
                    try {
                        await selectedPage.view.unmount()
                    } catch (e) {
                        console.error("Failed to unmount old page", selectedPage, e)
                    }
                newPage.div.removeAttribute("hidden");
                if (newPage.view)
                    try {
                        await newPage.view.mount(args)
                    } catch (e) {
                        console.error("Failed to mount new page", newPage, e)
                    }
                selectedPage = newPage;
            },
        }
        obj.setById = async (id, args) => {
            const newPage = pages.filter(p => p.id === id)[0]
            if (!newPage)
                throw "invalid page id " + id
            await obj.set(newPage, args);
        }
        // Trigger the initial page mount
        // TODO: we need to remove the home page and write the custom logic by saving client-sided what was visited
        obj.set(selectedPage)
        return obj
    })()

    const pagesMenu = document.getElementById('pages-menu')
    const pageLinkTemplate = document.getElementById('page-link-template')
    pages.forEach(page => {
        // If it doesn't have a display name, we are not supposed to show it
        if (!page.displayName)
            return

        const pageLink = pageLinkTemplate.cloneNode(true)
        /** @type HTMLElement */
        const anchor = pageLink.querySelector('.link-anchor')
        anchor.textContent = page.displayName
        anchor.addEventListener('click', () => selectedPage.set(page));
        Array.from(pageLink.childNodes).forEach(node => pagesMenu.appendChild(node))
    });

    //repositories
    const auctionRepository = (() => {
        const url = "http://localhost:8081/JS/"

        /**
         * @typedef {{ error: true, msg: string }} ErrorResponse
         * @typedef {{ error: false, offerId: number, userId: number, auctionId: number, price: number, name: string, date: Date}} Offer
         * @typedef {{error: false, codArticle: number, name: string, description: string, immagine: string, prezzo: number, idUtente: number }} Article
         * @typedef {{error: false, id: number, expiry: Date, articles: Article[], minimumOfferDifference: number, maxOffer: number }} Auction
         * @typedef {{error: false, kind: 'closed', base: Auction, finalPrice: number, buyerName: string, buyerAddress: string }} ClosedAuction
         * @typedef {{error: false, kind: 'open', base: Auction, offers: Offer[] }} OpenAuction
         */

        /**
         * @param {number} id
         * @return {Promise<ErrorResponse | OpenAuction | ClosedAuction>} result
         */
        const getAuctionByIds = async function (id) {
            const response = await fetch(url + 'auction?id=' + id)
            /** @type {ErrorResponse | Auction} */
            const obj = await response.json();
            if (!obj.error)
                obj.base.expiry = new Date(obj.base.expiry)
            return obj
        }

        /**
         * @return {Promise<ErrorResponse | Auction[]>} result
         */
        const getClosedAuction = async function () {
            const response = await fetch(url + 'closedAuction')
            /** @type {ErrorResponse | Auction[]} */
            const obj = await response.json();
            if (!obj.error)
                obj.map(a => {
                    a.expiry = new Date(a.expiry)
                    return a
                })
            return obj
        }

        /**
         * @return {Promise<ErrorResponse | Auction[]>} result
         */
        const getOpenAuction = async function () {
            const response = await fetch(url + 'openAuction')
            /** @type {ErrorResponse | Auction[]} */
            const obj = await response.json();
            if (!obj.error)
                obj.map(a => {
                    a.expiry = new Date(a.expiry)
                    return a
                })
            return obj
        }

        /**
         * @param {URLSearchParams} formData
         * @returns {Promise<any>}
         */
        const insertAuction = async function (formData) {
            const response = await fetch(url + 'auction', {
                method: 'POST',
                body: formData,
            })
            return await response.json();
        }

        /**
         * @param {string} keyWord
         * @returns {Promise<ErrorResponse | { error: false } & Auction[]>}
         */
        const searchAuction = async function (keyWord) {
            //TODO: check for % that returns everything
            const response = await fetch(url + "search?" + new URLSearchParams({search: keyWord}).toString())
            /** @type {ErrorResponse | { error: false } & Auction[]} */
            const res = await response.json();
            if (res.error)
                return res
            // noinspection UnnecessaryLocalVariableJS
            /** @type {any} */
            const mapped = res.map(a => {
                a.expiry = new Date(a.expiry)
                return a
            })
            return mapped
        }

        /**
         * @returns {Promise<ErrorResponse | {error: false} & ClosedAuction[]>}
         */
        const getBoughtAuctions = async function () {
            const response = await fetch(url + "auction")
            /** @type {ErrorResponse | { error: false } & ClosedAuction[]} */
            const res = await response.json();
            if (res.error)
                return res
            // noinspection UnnecessaryLocalVariableJS
            /** @type {any} */
            const mapped = res.map(a => {
                a.base.expiry = new Date(a.base.expiry)
                return a
            })
            return mapped
        }

        /**
         * @param {URLSearchParams} formData
         * @returns {Promise<ErrorResponse | any>}
         */
        const closeAuction = async function (formData) {
            const response = await fetch(url + "closedAuction", {
                method: 'POST',
                body: formData,
            })
            /** @type {ErrorResponse | any} */
            return await response.json();
        }

        return {
            closeAuction: closeAuction,
            getClosedAuction: getClosedAuction,
            getOpenAuction: getOpenAuction,
            getAuctionByIds: getAuctionByIds,
            insertAuction: insertAuction,
            searchAuction: searchAuction,
            getBoughtAuctions: getBoughtAuctions
        }
    })();
    const articleRepository = (() => {
        const url = "http://localhost:8081/JS/"

        /**
         * @typedef {{ error: true, msg: string }} ErrorResponse
         * @typedef {{error: false, codArticle: number, name: string, description: string, immagine: string, prezzo: number, idUtente: number }} Article
         */

        /**
         * @param {FormData} formData
         * @returns {Promise<any>}
         */
        const insertArticle = async function (formData) {
            const response = await fetch(url + 'article', {
                method: 'POST',
                body: formData,
            })
            return await response.json();
        }

        /**
         * @returns {Promise<ErrorResponse | {error: false} & Article[]>}
         */
        const findAllArticles = async function () {
            const response = await fetch(url + 'article')
            /** @type {ErrorResponse | { error: false } & Article[]} */
            return await response.json();
        }

        return {
            insertArticle: insertArticle,
            findAllArticles: findAllArticles,
        }
    })();

    //pages
    function buyPage(containerDiv) {

        const auctionTemplate = document.getElementById('found-auction-template')
        const articleTemplate = document.getElementById('article-template')

        const wonAuctionTemplate = document.getElementById('won-auction-template')
        const wonArticleTemplate = document.getElementById('won-auction-article-template')
        const wonAuctionTable = document.getElementById('won-auction-table')

        const foundAuctionsTable = document.getElementById("found-auctions-container");
        const keyword = document.getElementById("search-keyword")

        const errorSearchQuery = document.getElementById("errorSearchQuery");

        this.create = async () => {
            // Called once when the page is first loaded
            // Register event listeners for elements that are not dynamic

            const form = document.getElementById('search-form')
            form.addEventListener('submit', async e => {
                e.preventDefault()

                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }

                await this.mutateState()
            })
        };

        this.mount = async () => {
            // Called when a page is shown
            // Refresh data we got from the repo
            const promises = []

            if (keyword.value.trim() !== "") {
                // Load from repo
                promises.push(auctionRepository.searchAuction(keyword.value).then(auctions => {
                    // Once loaded, we can clean up old cached data
                    while (foundAuctionsTable.firstChild)
                        foundAuctionsTable.removeChild(foundAuctionsTable.firstChild)

                    if (auctions.error) {
                        errorSearchQuery.removeAttribute("hidden");
                    } else {
                        errorSearchQuery.setAttribute("hidden", "");
                        auctions.forEach(auction => {
                            const auctionEl = auctionTemplate.cloneNode(true)
                            auctionEl.querySelector('.auction-id').textContent = auction.id
                            auctionEl.querySelector('.auction-maxOffer').textContent = auction.maxOffer
                            // TODO: we need to use the login time, not a new date
                            const dateDiffMillis = auction.expiry - new Date()
                            const days = Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24);
                            const hours = Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24);
                            auctionEl.querySelector('.auction-remaining-time').textContent = `${days}d ${hours}h`

                            /** @type {HTMLElement} */
                            const articleTable = auctionEl.querySelector('.article-table')
                            auction.articles.forEach(article => {
                                const articleEl = articleTemplate.cloneNode(true)
                                articleEl.querySelector('.article-code').textContent = article.codArticle
                                articleEl.querySelector('.article-name').textContent = article.name
                                Array.from(articleEl.childNodes).forEach(node => articleTable.appendChild(node));
                            })

                            Array.from(auctionEl.childNodes).forEach(node => foundAuctionsTable.appendChild(node));
                        })
                    }
                }))
            }

            promises.push(auctionRepository.getBoughtAuctions().then(wonAuctions => {
                // Once loaded, we can clean up old cached data
                while (wonAuctionTable.firstChild)
                    wonAuctionTable.removeChild(wonAuctionTable.firstChild)

                //TODO: error message
                if (wonAuctions.error) {
                    console.error("nothing found")
                } else {
                    wonAuctions.forEach(wonAuction => {
                        const wonAuctionEl = wonAuctionTemplate.cloneNode(true)
                        wonAuctionEl.querySelector('.won-auction-id').textContent = wonAuction.base.id
                        wonAuctionEl.querySelector('.won-auction-price').textContent = wonAuction.finalPrice

                        /** @type {HTMLElement} */
                        const wonArticleTable = wonAuctionEl.querySelector('.won-article-table')
                        wonAuction.base.articles.forEach(wonArticle => {
                            const wonArticleEl = wonArticleTemplate.cloneNode(true)
                            wonArticleEl.querySelector('.won-article-id').textContent = wonArticle.codArticle
                            wonArticleEl.querySelector('.won-article-name').textContent = wonArticle.name
                            Array.from(wonArticleEl.childNodes).forEach(node => wonArticleTable.appendChild(node))
                        })

                        Array.from(wonAuctionEl.childNodes).forEach(node => wonAuctionTable.appendChild(node));
                    })
                }
            }))

            await Promise.all(promises)
        }

        this.unmount = async () => {
            // Called when a page is no longer shown
            // Do anything that might be required when the page is removed
        }

        this.mutateState = async () => {
            // Easiest way to mutate is to just unmount and remount
            await this.unmount()
            await this.mount()
        }
    }

    function sellPage(containerDiv) {
        const articleForm = document.getElementById("article-insertion-form")
        const auctionForm = document.getElementById("auction-insertion-form")
        const articlesErrorQuery = document.getElementById("articles-error-query")
        const articlesNotFound = document.getElementById("articles-not-found")
        const selectArticles = document.getElementById("selectedArticles")

        const closedAuctionTemplate = document.getElementById("closed-auction-template")
        const closedAuctionTable = document.getElementById("closed-auction-table")
        const closedArticleTemplate = document.getElementById("closed-article-template")
        const sellClosedErrorQuery = document.getElementById("sellClosedErrorQuery")

        const openAuctionTemplate = document.getElementById("open-auction-template")
        const openAuctionTable = document.getElementById("open-auctions-table")
        const openArticleTemplate = document.getElementById("open-article-template")
        const sellOpenErrorQuery = document.getElementById("sellOpenErrorQuery")

        this.create = async () => {
            articleForm.addEventListener('submit', async e => {
                e.preventDefault()
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                await articleRepository.insertArticle(new FormData(articleForm))
                e.target.reset()
                await this.mutateState()
            })

            auctionForm.addEventListener('submit', async e => {
                e.preventDefault()
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                //noinspection JSCheckFunctionSignatures
                await auctionRepository.insertAuction(new URLSearchParams(new FormData(auctionForm)))
                e.target.reset()
                await this.mutateState()
            })
        };

        this.mount = async () => {
            // Called when a page is shown
            // Refresh data we got from the repo
            const promises = []

            promises.push(articleRepository.findAllArticles().then(articles => {
                // Once loaded, we can clean up old cached data
                while (selectArticles.firstChild)
                    selectArticles.removeChild(selectArticles.firstChild)

                if (articles.error) {
                    articlesErrorQuery.removeAttribute("hidden")
                    return
                }
                articlesErrorQuery.setAttribute("hidden", "")

                if (articles.length === 0) {
                    articlesNotFound.removeAttribute("hidden")
                    return
                }
                articlesNotFound.setAttribute("hidden", "")

                articles.forEach(article => {
                    const articleOptionEl = document.createElement("option")
                    articleOptionEl.value = article.codArticle.toString()
                    articleOptionEl.textContent = article.name
                    selectArticles.appendChild(articleOptionEl);
                })
            }))

            //populates closed auction tables
            promises.push(auctionRepository.getClosedAuction().then(closedAuctions => {
                // Once loaded, we can clean up old cached data
                while (closedAuctionTable.firstChild)
                    closedAuctionTable.removeChild(closedAuctionTable.firstChild)

                if (closedAuctions.error) {
                    sellClosedErrorQuery.removeAttribute("hidden")
                    return
                }

                sellClosedErrorQuery.setAttribute("hidden", "")
                closedAuctions.forEach(closedAuction => {
                    const closedAuctionEl = closedAuctionTemplate.cloneNode(true)
                    /** @type {HTMLElement} */
                    const closedAuctionAnchor = closedAuctionEl.querySelector('.closed-auction-id')
                    closedAuctionAnchor.textContent = closedAuction.id
                    closedAuctionAnchor.addEventListener('click', async e => {
                        e.preventDefault()
                        await selectedPage.setById('auction-details', closedAuction.id)
                    })
                    closedAuctionEl.querySelector('.closed-auction-final-price').textContent = closedAuction.maxOffer

                    /** @type {HTMLElement} */
                    const closedArticleTable = closedAuctionEl.querySelector('.closed-article-table')
                    closedAuction.articles.forEach(closedArticle => {
                        const closedArticleEl = closedArticleTemplate.cloneNode(true)
                        closedArticleEl.querySelector('.closed-article-id').textContent = closedArticle.codArticle
                        closedArticleEl.querySelector('.closed-article-name').textContent = closedArticle.name
                        Array.from(closedArticleEl.childNodes).forEach(node => closedArticleTable.appendChild(node))
                    })

                    Array.from(closedAuctionEl.childNodes).forEach(node => closedAuctionTable.appendChild(node));
                })
            }))

            //populates closed auction tables
            promises.push(auctionRepository.getOpenAuction().then(openAuctions => {
                // Once loaded, we can clean up old cached data
                while (openAuctionTable.firstChild)
                    openAuctionTable.removeChild(openAuctionTable.firstChild)

                if (openAuctions.error) {
                    sellOpenErrorQuery.removeAttribute("hidden")
                    return
                }

                sellOpenErrorQuery.setAttribute("hidden", "")
                openAuctions.forEach(openAuction => {
                    const openAuctionEl = openAuctionTemplate.cloneNode(true)
                    /** @type {HTMLElement} */
                    const openAuctionAnchor = openAuctionEl.querySelector('.open-auction-id')
                    openAuctionAnchor.textContent = openAuction.id
                    openAuctionAnchor.addEventListener('click', async e => {
                        e.preventDefault()
                        await selectedPage.setById('auction-details', openAuction.id)
                    })
                    openAuctionEl.querySelector('.open-auction-max-offer').textContent = openAuction.maxOffer
                    // TODO: we need to use the login time, not a new date
                    const dateDiffMillis = openAuction.expiry - new Date()
                    const days = Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24);
                    const hours = Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24);
                    openAuctionEl.querySelector('.open-auction-remaining-time').textContent = `${days}d ${hours}h`

                    /** @type {HTMLElement} */
                    const openArticleTable = openAuctionEl.querySelector('.open-articles-table')
                    openAuction.articles.forEach(openArticle => {
                        const openArticleEl = openArticleTemplate.cloneNode(true)
                        openArticleEl.querySelector('.open-article-id').textContent = openArticle.codArticle
                        openArticleEl.querySelector('.open-article-name').textContent = openArticle.name
                        Array.from(openArticleEl.childNodes).forEach(node => openArticleTable.appendChild(node))
                    })

                    Array.from(openAuctionEl.childNodes).forEach(node => openAuctionTable.appendChild(node));
                })
            }))

            await Promise.all(promises)
        };

        this.unmount = async () => {
            // Called when a page is no longer shown
            // Do anything that might be required when the page is removed
        }

        this.mutateState = async () => {
            // Easiest way to mutate is to just unmount and remount
            await this.unmount()
            await this.mount()
        }
    }

    function auctionDetailsPage(containerDiv) {
        let currentId = undefined

        const auctionToCloseInput = document.getElementById("auction-to-close-id")
        const closeAuctionForm = document.getElementById("close-button-form")
        const auctionDetailsErrorQuery = document.getElementById("auction-details-error-query")
        const auctionDetailsExpiration = document.getElementById("auction-details-expiry-div")
        const auctionCloseButton = document.getElementById("auction-details-close-button")
        const auctionDetailsContent = document.getElementById("auction-details-content")
        const auctionDetailsIdEl = document.getElementById("auction-details-id")
        const articleDetailsTemplate = document.getElementById("articles-details-template")
        const articleDetailsContainer = document.getElementById("articles-details-container")

        const closedAuctionDetails = document.getElementById("closed-auction-details")
        const closedAuctionFinalPrice = document.getElementById("closed-auction-final-price")
        const closedAuctionBuyer = document.getElementById("closed-auction-buyer")
        const closedAuctionAddress = document.getElementById("closed-auction-buyer-address")

        const openAuctionDetails = document.getElementById("open-auction-details")
        const openAuctionDetailsTemplate = document.getElementById("auction-details-offers-template")
        const openAuctionDetailsContainer = document.getElementById("auction-details-offers-table")

        this.create = async () => {
            closeAuctionForm.addEventListener('submit', async e => {
                e.preventDefault()
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                await auctionRepository.closeAuction(new URLSearchParams(new FormData(closeAuctionForm)))
                e.target.reset()
                await this.mutateState()
            })
        };

        /**
         * @param {number} id
         * @returns {Promise<void>}
         */
        this.mount = async (id) => {
            currentId = id

            if (!id) {
                auctionDetailsErrorQuery.removeAttribute("hidden")
                auctionDetailsContent.setAttribute("hidden", "")
                return
            }
            auctionDetailsErrorQuery.setAttribute("hidden", "")
            auctionDetailsContent.removeAttribute("hidden")

            const auction = await auctionRepository.getAuctionByIds(id)
            if (auction.error) {
                auctionDetailsErrorQuery.removeAttribute("hidden")
                auctionDetailsContent.setAttribute("hidden", "")
                return
            }

            auctionDetailsIdEl.textContent = auction.base.id.toString()

            while (articleDetailsContainer.firstChild)
                articleDetailsContainer.removeChild(articleDetailsContainer.firstChild)

            auction.base.articles.forEach(article => {
                const articleDetailsEl = articleDetailsTemplate.cloneNode(true)
                articleDetailsEl.querySelector('.article-details-code').textContent = article.codArticle
                articleDetailsEl.querySelector('.article-details-name').textContent = article.name
                articleDetailsEl.querySelector('.article-details-description').textContent = article.description
                articleDetailsEl.querySelector('.article-details-image').src = "data:image/jpeg;base64," + article.immagine

                Array.from(articleDetailsEl.childNodes).forEach(node => articleDetailsContainer.appendChild(node));
            });

            if (auction.kind === 'open') {
                if (auction.base.expiry - new Date() <= 0) {
                    auctionToCloseInput.value = id.toString();
                    auctionCloseButton.removeAttribute("hidden")
                    auctionDetailsExpiration.setAttribute("hidden", "")
                } else {
                    auctionCloseButton.setAttribute("hidden", "")
                    auctionDetailsExpiration.removeAttribute("hidden")
                }
                openAuctionDetails.removeAttribute("hidden")
                closedAuctionDetails.setAttribute("hidden", "")

                while (openAuctionDetailsContainer.firstChild)
                    openAuctionDetailsContainer.removeChild(openAuctionDetailsContainer.firstChild)

                auction.offers.forEach(offer => {
                    const offerEl = openAuctionDetailsTemplate.cloneNode(true)
                    offerEl.querySelector('.details-offer-date').textContent = new Date(offer.date)
                    offerEl.querySelector('.details-offer-name').textContent = offer.name
                    offerEl.querySelector('.details-offer-price').textContent = offer.price

                    Array.from(offerEl.childNodes).forEach(node => openAuctionDetailsContainer.appendChild(node));
                })

                return
            }

            // if(auction.kind === 'closed')
            auctionCloseButton.setAttribute("hidden", "")
            auctionDetailsExpiration.setAttribute("hidden", "")
            openAuctionDetails.setAttribute("hidden", "")
            closedAuctionFinalPrice.textContent = auction.finalPrice.toString()
            closedAuctionBuyer.textContent = auction.buyerName
            closedAuctionAddress.textContent = auction.buyerAddress
            closedAuctionDetails.removeAttribute("hidden")
        };

        this.unmount = async () => {
            // Called when a page is no longer shown
            // Do anything that might be required when the page is removed
            // sets everything to hidden while page is loading
            auctionDetailsContent.setAttribute("hidden", "")
            closedAuctionDetails.setAttribute("hidden", "")
            openAuctionDetails.setAttribute("hidden", "")
        }

        this.mutateState = async (id) => {
            // Easiest way to mutate is to just unmount and remount
            await this.unmount()
            await this.mount(id || currentId)
        }
    }
});