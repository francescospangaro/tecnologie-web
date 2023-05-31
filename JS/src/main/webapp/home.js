document.addEventListener('DOMContentLoaded', async () => {

    const webappPathnamePrefix = "JS"
    const url = ""

    /**
     * @param {RequestInfo | URL} input
     * @param {RequestInit | undefined} [init=undefined]
     * @return {Promise<Response>}
     */
    const fetchIfAuthenticated = async (input, init) => {
        const res = await fetch(input, init);
        if (res.status === 401)
            document.location = "login"
        return res;
    };

    //repositories
    const auctionRepository = (() => {

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
            const response = await fetchIfAuthenticated(url + 'auction?id=' + id)
            /** @type {ErrorResponse | OpenAuction | ClosedAuction} */
            const obj = await response.json();
            if (!obj.error) {
                obj.base.expiry = new Date(obj.base.expiry)
                if (obj.kind === 'open')
                    obj.offers = obj.offers.map(o => {
                        o.date = new Date(o.date)
                        return o;
                    })
            }
            return obj
        }

        /**
         * @return {Promise<ErrorResponse | Auction[]>} result
         */
        const getClosedAuction = async function () {
            const response = await fetchIfAuthenticated(url + 'closedAuction')
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
            const response = await fetchIfAuthenticated(url + 'openAuction')
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
         * @return {Promise<ErrorResponse | OpenAuction>} result
         */
        const getOpenAuctionById = async function (id) {
            const response = await fetchIfAuthenticated(url + 'offer?' + new URLSearchParams({
                id: id
            }).toString())
            /** @type {ErrorResponse | OpenAuction} */
            const obj = await response.json();
            if (!obj.error) {
                obj.base.expiry = new Date(obj.base.expiry)
                obj.offers = obj.offers.map(o => {
                    o.date = new Date(o.date)
                    return o
                })
            }
            return obj
        }

        /**
         * @param {URLSearchParams} formData
         * @returns {Promise<ErrorResponse | any>}
         */
        const insertAuction = async function (formData) {
            const response = await fetchIfAuthenticated(url + 'auction', {
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
            const response = await fetchIfAuthenticated(url + "search?" + new URLSearchParams({search: keyWord}).toString())
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
            const response = await fetchIfAuthenticated(url + "auction")
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
            const response = await fetchIfAuthenticated(url + "closedAuction", {
                method: 'POST',
                body: formData,
            })
            /** @type {ErrorResponse | any} */
            return await response.json();
        }

        return {
            getOpenAuctionById: getOpenAuctionById,
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

        /**
         * @typedef {{ error: true, msg: string }} ErrorResponse
         * @typedef {{error: false, codArticle: number, name: string, description: string, immagine: string, prezzo: number, idUtente: number }} Article
         */

        /**
         * @param {FormData} formData
         * @returns {Promise<ErrorResponse | any>}
         */
        const insertArticle = async function (formData) {
            const response = await fetchIfAuthenticated(url + 'article', {
                method: 'POST',
                body: formData,
            })
            return await response.json();
        }

        /**
         * @returns {Promise<ErrorResponse | {error: false} & Article[]>}
         */
        const findAllArticles = async function () {
            const response = await fetchIfAuthenticated(url + 'article')
            /** @type {ErrorResponse | { error: false } & Article[]} */
            return await response.json();
        }

        return {
            insertArticle: insertArticle,
            findAllArticles: findAllArticles,
        }
    })();
    const offerRepository = (() => {
        /**
         * @typedef {{ error: true, msg: string }} ErrorResponse
         * @typedef {{error: false, offerId: number, userId: number, auctionId: number, price: number, name: string, date: Date }} Offer
         */

        /**
         * @param {FormData} formData
         * @returns {Promise<ErrorResponse | any>}
         */
        const insertOffer = async function (formData) {
            const response = await fetchIfAuthenticated(url + 'offer', {
                method: 'POST',
                body: formData,
            })
            return await response.json();
        }

        return {
            insertOffer: insertOffer
        }
    })();

    // Get session data
    /**
     * @typedef {{ id: number, name: string, loginTime: Date }} UserSession
     */

    /** @type {UserSession | undefined} */
    const user = !localStorage.getItem('user')
        ? undefined
        : /** @type {UserSession} */ JSON.parse(localStorage.getItem('user'))
    if (!user) {
        document.location = "login"
        return
    }

    user.loginTime = new Date(user.loginTime)

    // Handle interactions state

    /**
     * @typedef {"inserted-auction" | "inserted-article" | "placed-offer" | "searched-auctions" | ""} UserAction
     * @typedef {{ date: Date, lastAction: UserAction, visitedAuctions: { id: number, date: Date }[] }} UserInteractions
     * @typedef {{ [id: string]: UserInteractions }} SavedInteractions
     */

    const userInteractions = (() => {
        const oneMonthInMillis = 1000 * 60 * 60 * 24 * 30

        const savedInteractionsStr = localStorage.getItem('interactions')
        /** @type SavedInteractions */
        const savedInteractions = savedInteractionsStr ? /** @type SavedInteractions */ JSON.parse(savedInteractionsStr) : {}

        const firstTime = !savedInteractions
            || !savedInteractions[user.id]
            // If the last saved interaction is older than 1 month, discard it
            || new Date() - new Date(savedInteractions[user.id].date) > oneMonthInMillis;
        const interactions = (() => {
            /** @type UserInteractions */
            let __interactions = !firstTime
                ? savedInteractions[user.id]
                : {
                    lastAction: "",
                    visitedAuctions: []
                }
            // Fix JSON parsed dates
            __interactions.date = new Date(__interactions.date)
            __interactions.visitedAuctions = __interactions.visitedAuctions.map(a => {
                a.date = new Date(a.date)
                return a
            })
            const obj = {
                /** @return {UserInteractions} */
                get: () => __interactions,
                /** @param {UserInteractions} newInteractions */
                set: (newInteractions) => {
                    __interactions = newInteractions
                    savedInteractions[user.id] = newInteractions
                    localStorage.setItem('interactions', JSON.stringify(savedInteractions))
                },
                /** @param {(UserInteractions) => UserInteractions} fn */
                update: (fn) => {
                    obj.set(fn(obj.get()))
                }
            }
            return obj
        })()
        // Save either the new or the filtered interactions to localStorage
        interactions.update(i => {
            return /** @type UserInteractions */ {
                ...i,
                visitedAuctions: i.visitedAuctions.filter(a => new Date() - new Date(a.date) <= oneMonthInMillis)
            }
        })

        return {
            isFirstTime: () => firstTime,
            getLastAction: () => interactions.get().lastAction,
            /** @type {(UserAction) => void} */
            setLastAction: (action) => interactions.update(i => {
                return /** @type UserInteractions */ {
                    ...i,
                    lastAction: action,
                    date: new Date(),
                }
            }),
            getVisitedAuctions: () => interactions.get().visitedAuctions,
            /** @type {(number) => void} */
            addVisitedAuction: (id) => interactions.update(i => {
                return /** @type UserInteractions */ {
                    ...i,
                    visitedAuctions: [...i.visitedAuctions, {
                        id: id,
                        date: new Date()
                    }],
                    date: new Date(),
                }
            }),
        };
    })()

    // Router

    /**
     * @typedef {{ create: (HTMLElement) => Promise<any>, mount: (URLSearchParams) => Promise<any>, unmount: () => Promise<any> }} PageView
     * @typedef {{ id: string, displayName?: string, div: HTMLElement, view?: PageView}} Page
     */

    /** @type {Page[]} */
    const pages = await Promise.all((() => {
        return [
            {id: "buy", displayName: "Buy", div: document.getElementById("buy-page"), view: buyPage},
            {id: "sell", displayName: "Sell", div: document.getElementById("sell-page"), view: sellPage},
            {id: "auctionDetails", div: document.getElementById("auction-details-page"), view: auctionDetailsPage},
            {id: "offers", div: document.getElementById("offers-page"), view: offersPage},
        ].map(async page => {
            const view = page.view ? new page.view(page.div) : undefined
            if (view)
                try {
                    await view.create()
                } catch (e) {
                    console.error("Failed to create page view", page, e)
                }
            page.view = view
            return /** @type {Page} */ page
        })
    })());

    /**
     * @typedef {{
     *      set: (Page, args?: URLSearchParams) => Promise<void>,
     *      setById: (string, args?: URLSearchParams) => Promise<void>,
     *      get: () => Page,
     *      isHomePage: () => boolean
     * }} Router
     */

    /** @type Router */
    const router = await (async () => {
        /** @type {Page | undefined} */
        let selectedPage = undefined;

        const getFixedPathName = () => {
            return document.location.pathname.startsWith(`/${webappPathnamePrefix}`)
                ? document.location.pathname.substring(`/${webappPathnamePrefix}`.length)
                : document.location.pathname
        }

        const isHomePathname = (pathname) => {
            return pathname === "/home" || pathname === "/home.html"
        }

        const triggerPageChange = async (newPage, args) => {
            if (selectedPage) {
                selectedPage.div.setAttribute("hidden", "");
                if (selectedPage.view)
                    try {
                        await selectedPage.view.unmount()
                    } catch (e) {
                        console.error("Failed to unmount old page", selectedPage, e)
                    }
            }

            newPage.div.removeAttribute("hidden");
            if (newPage.view)
                try {
                    await newPage.view.mount(args || new URLSearchParams())
                } catch (e) {
                    console.error("Failed to mount new page", newPage, e)
                }

            selectedPage = newPage;
        }

        /**
         * @param {{pageId: string, args: URLSearchParams} | undefined} [state=undefined]
         * @return {Promise<void>}
         */
        const doRoute = async (state) => {
            let fixedPathName = getFixedPathName();
            if (isHomePathname(fixedPathName)) {
                fixedPathName = "/home" // Remove .html suffix

                if (userInteractions.isFirstTime())
                    fixedPathName = "/buy"
                else if(userInteractions.getLastAction() === "inserted-auction")
                    fixedPathName = "/sell"
                else
                    fixedPathName = "/buy"
            }

            const pageId = state ? state.pageId : fixedPathName.substring('/'.length)
            if (selectedPage && pageId === selectedPage.id)
                return

            const args = state ? state.args : new URLSearchParams(document.location.search)
            const newPage = pages.filter(p => p.id === pageId)[0]
            if (!newPage) {
                console.error('Invalid page transition for id', pageId, ". State", state)
                return
            }

            await triggerPageChange(newPage, args)
        }

        // Setup event listener for routing
        addEventListener("popstate", async (event) => {
            await doRoute(event.state && event.state.didIPushTheState ? event.state : undefined)
        });

        // Trigger the initial page mount
        // Do it in a microtask, so that it will be executed after the router object is created
        queueMicrotask(doRoute)

        /** @type {Router} */
        const obj = {
            get: () => {
                return /** @type {Page} */selectedPage
            },
            set: async (newPage, args) => {
                history.pushState({didIPushTheState: true, pageId: newPage.id, args: args}, '', args
                    ? newPage.id + '?' + args.toString()
                    : newPage.id)
                await triggerPageChange(newPage, args)
            },
            setById: async (id, args) => {
                const newPage = pages.filter(p => p.id === id)[0]
                if (!newPage)
                    throw "invalid page id " + id
                await obj.set(newPage, args);
            },
            isHomePage() {
              return isHomePathname(getFixedPathName())
            },
        }
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
        anchor.addEventListener('click', () => router.set(page));
        Array.from(pageLink.childNodes).forEach(node => pagesMenu.appendChild(node))
    });

    // pages view

    function buyPage() {

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

                userInteractions.setLastAction("searched-auctions")
                await router.setById('buy', new URLSearchParams({search: keyword.value}))
            })
        };

        /**
         * @param {URLSearchParams} args
         * @return {Promise<void>}
         */
        this.mount = async (args) => {
            // Called when a page is shown
            // Refresh data we got from the repo
            const promises = []

            const keyword = args.get('search')
            // Load from repo
            promises.push((
                keyword && keyword.trim() !== ""
                    ? auctionRepository.searchAuction(keyword)
                    : router.isHomePage()
                        ? Promise.all(userInteractions.getVisitedAuctions()
                            .map(async id => {
                                const res = await auctionRepository.getOpenAuctionById(id)
                                if(res.error) {
                                    console.error("Failed to load already visited auction ", id, res);
                                    return undefined
                                }

                                return res
                            })
                            // Remove undefined ones from the list
                            // We need to await it as the map() call returns a Promise<OpenAuction | undefined>[]
                            // because we pass it an async mapping function
                            .filter(async auction => await auction)
                            // we need to wait for the promise response at every step
                            .map(async auction => (await auction).base))
                        : new Promise(resolve => resolve([]))
            ).then(auctions => {
                // Once loaded, we can clean up old cached data
                while (foundAuctionsTable.firstChild)
                    foundAuctionsTable.removeChild(foundAuctionsTable.firstChild)

                if (auctions.error) {
                    errorSearchQuery.removeAttribute("hidden");
                } else {
                    errorSearchQuery.setAttribute("hidden", "");
                    auctions.forEach(auction => {
                        const auctionEl = auctionTemplate.cloneNode(true)
                        /** @type {HTMLElement} */
                        const auctionAnchor = auctionEl.querySelector('.auction-id')
                        auctionAnchor.textContent = auction.id
                        auctionAnchor.addEventListener('click', async e => {
                            e.preventDefault()
                            await router.setById('offers', new URLSearchParams({
                                id: auction.id
                            }))
                        })
                        auctionEl.querySelector('.auction-maxOffer').textContent = auction.maxOffer
                        const dateDiffMillis = auction.expiry - user.loginTime
                        const days = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24);
                        const hours = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24);
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

            promises.push(auctionRepository.getBoughtAuctions().then(wonAuctions => {
                // Once loaded, we can clean up old cached data
                while (wonAuctionTable.firstChild)
                    wonAuctionTable.removeChild(wonAuctionTable.firstChild)

                if (wonAuctions.error) {
                    console.error("Failed to load bought auctions", wonAuctions)
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
            await this.mount(new URLSearchParams())
        }
    }

    function sellPage() {
        const articleForm = document.getElementById("article-insertion-form")
        const auctionForm = document.getElementById("auction-insertion-form")
        const articlesErrorQuery = document.getElementById("articles-error-query")
        const articlesNotFound = document.getElementById("articles-not-found")
        const selectArticles = document.getElementById("selectedArticles")

        const errorAuctionData = document.getElementById("auctionDataError")
        const errorAuctionQuery = document.getElementById("errorAuctionQuery")

        const errorArticleInsertion = document.getElementById("errorArticleInsertion")
        const errorArticleQuery = document.getElementById("errorArticleQuery")

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
                errorArticleInsertion.setAttribute("hidden", "")
                errorArticleQuery.setAttribute("hidden", "")
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                //noinspection JSCheckFunctionSignatures
                const res = await articleRepository.insertArticle(new FormData(articleForm))
                if (res.error) {
                    if (res.msg === "errorArticleDataInserted") {
                        errorArticleInsertion.removeAttribute("hidden")
                    } else {
                        errorArticleQuery.removeAttribute("hidden")
                    }
                    return
                }
                userInteractions.setLastAction("inserted-article")
                e.target.reset()
                await this.mutateState()
            })

            auctionForm.addEventListener('submit', async e => {
                e.preventDefault()
                errorAuctionData.setAttribute("hidden", "")
                errorAuctionQuery.setAttribute("hidden", "")
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                //noinspection JSCheckFunctionSignatures
                const res = await auctionRepository.insertAuction(new URLSearchParams(new FormData(auctionForm)))
                if (res.error) {
                    if (res.msg === 'errorAuctionDataInserted') {
                        errorAuctionData.removeAttribute("hidden")
                    } else {
                        errorAuctionQuery.removeAttribute("hidden")
                    }
                    return
                }
                userInteractions.setLastAction("inserted-auction")
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

                articlesNotFound.setAttribute("hidden", "")
                articlesErrorQuery.setAttribute("hidden", "")

                if (articles.error) {
                    articlesErrorQuery.removeAttribute("hidden")
                    return
                }

                if (articles.length === 0) {
                    articlesNotFound.removeAttribute("hidden")
                    return
                }

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
                        await router.setById('auctionDetails', new URLSearchParams({
                            id: closedAuction.id
                        }))
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
                        await router.setById('auctionDetails', new URLSearchParams({
                            id: openAuction.id
                        }))
                    })
                    openAuctionEl.querySelector('.open-auction-max-offer').textContent = openAuction.maxOffer
                    const dateDiffMillis = openAuction.expiry - user.loginTime
                    const days = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24);
                    const hours = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24);
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

    function auctionDetailsPage() {
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
        const openAuctionExpiryDays = document.getElementById("open-auction-expiry-days")
        const openAuctionExpiryHours = document.getElementById("open-auction-expiry-hours")

        this.create = async () => {
            closeAuctionForm.addEventListener('submit', async e => {
                e.preventDefault()
                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }
                //noinspection JSCheckFunctionSignatures
                await auctionRepository.closeAuction(new URLSearchParams(new FormData(closeAuctionForm)))
                e.target.reset()
                await this.mutateState()
            })
        };

        /**
         * @param {URLSearchParams} params
         * @returns {Promise<void>}
         */
        this.mount = async (params) => {
            const id = parseInt(params.get('id'), 10)
            currentId = id

            if (!id) {
                auctionDetailsErrorQuery.removeAttribute("hidden")
                auctionDetailsContent.setAttribute("hidden", "")
                return
            }
            auctionDetailsErrorQuery.setAttribute("hidden", "")
            auctionDetailsContent.setAttribute("hidden", "")

            const auction = await auctionRepository.getAuctionByIds(id)
            if (auction.error) {
                auctionDetailsErrorQuery.removeAttribute("hidden")
                auctionDetailsContent.setAttribute("hidden", "")
                return
            }

            // Wait until we loaded content to make the div visible
            auctionDetailsContent.removeAttribute("hidden")
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
                const now = new Date()
                if (auction.base.expiry - now <= 0) {
                    auctionToCloseInput.value = id.toString();
                    auctionCloseButton.removeAttribute("hidden")
                    auctionDetailsExpiration.setAttribute("hidden", "")
                } else {
                    const dateDiffMillis = auction.base.expiry - user.loginTime
                    openAuctionExpiryDays.textContent = Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24).toString()
                    openAuctionExpiryHours.textContent = Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24).toString()
                    auctionDetailsExpiration.removeAttribute("hidden")
                    auctionCloseButton.setAttribute("hidden", "")
                }
                openAuctionDetails.removeAttribute("hidden")
                closedAuctionDetails.setAttribute("hidden", "")

                while (openAuctionDetailsContainer.firstChild)
                    openAuctionDetailsContainer.removeChild(openAuctionDetailsContainer.firstChild)

                auction.offers.forEach(offer => {
                    const offerEl = openAuctionDetailsTemplate.cloneNode(true)
                    offerEl.querySelector('.details-offer-date').textContent = offer.date.toLocaleString()
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
            await this.mount(new URLSearchParams({
                id: id || currentId
            }))
        }
    }

    function offersPage() {
        let currentId = undefined

        const offerErrorQuery = document.getElementById("offers-error-query")
        const offerErrorMax = document.getElementById("offers-error-max-offer")
        const offerLowPrice = document.getElementById("offers-error-low-price")
        const offersContent = document.getElementById("offers-content")
        const placeOfferForm = document.getElementById("offers-place-offer-form")

        const auctionIdEl = document.getElementById("offers-auction-id")
        const auctionIdInputEl = document.getElementById("offers-auction-id-input")
        const auctionExpiryDaysEl = document.getElementById("offers-auction-expiry-days")
        const auctionExpiryHoursEl = document.getElementById("offers-auction-expiry-hours")
        const auctionMaxPriceEl = document.getElementById("offers-auction-max-price")
        const auctionMinDiffEl = document.getElementById("offers-auction-minimum-diff")

        const offersTable = document.getElementById("offers-offers-table")
        const offerTemplate = document.getElementById("offers-offer-template")

        const articleContainer = document.getElementById("offers-article-container")
        const articleTemplate = document.getElementById("offers-article-template")

        this.create = async function () {
            placeOfferForm.addEventListener('submit', async e => {
                e.preventDefault()

                offerErrorQuery.setAttribute("hidden", "")
                offerErrorMax.setAttribute("hidden", "")
                offerLowPrice.setAttribute("hidden", "")

                if (!e.target.checkValidity()) {
                    e.target.reportValidity()
                    return
                }

                //noinspection JSCheckFunctionSignatures
                const res = await offerRepository.insertOffer(new URLSearchParams(new FormData(placeOfferForm)))
                if (res.error) {
                    if (res.msg === 'errorLowPrice') {
                        offerLowPrice.removeAttribute("hidden")
                    } else if (res.msg === 'errorMaxOffer') {
                        offerErrorMax.removeAttribute("hidden")
                    } else {
                        offerErrorQuery.removeAttribute("hidden")
                    }
                    return
                }

                userInteractions.setLastAction("placed-offer")
                e.target.reset()
                await this.mutateState()
            })
        }

        /**
         * @param {URLSearchParams} params
         * @returns {Promise<void>}
         */
        this.mount = async (params) => {
            const id = parseInt(params.get('id'), 10)
            currentId = id

            offersContent.setAttribute("hidden", "")
            offerErrorQuery.setAttribute("hidden", "")

            if (!id) {
                offerErrorQuery.removeAttribute("hidden")
                return
            }

            const auction = await auctionRepository.getOpenAuctionById(id)
            if (auction.error) {
                offerErrorQuery.removeAttribute("hidden")
                return
            }

            // Wait until we loaded content to make the div visible
            offersContent.removeAttribute("hidden")
            // No errors found, we can consider this as visited
            userInteractions.addVisitedAuction(id)

            auctionIdEl.textContent = auction.base.id.toString()
            auctionIdInputEl.value = auction.base.id.toString()

            const dateDiffMillis = auction.base.expiry - user.loginTime
            auctionExpiryDaysEl.textContent = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) / 24).toString()
            auctionExpiryHoursEl.textContent = dateDiffMillis < 0 ? 0 : Math.trunc(dateDiffMillis / (1000 * 60 * 60) % 24).toString()

            auctionMaxPriceEl.textContent = auction.base.maxOffer.toFixed(2)
            auctionMinDiffEl.textContent = auction.base.minimumOfferDifference.toFixed(2)

            // empties table from old data
            while (offersTable.firstChild)
                offersTable.removeChild(offersTable.firstChild)

            auction.offers.forEach(offer => {
                const offerEl = offerTemplate.cloneNode(true)
                offerEl.querySelector('.offers-offer-date').textContent = offer.date.toLocaleString()
                offerEl.querySelector('.offers-offer-user').textContent = offer.name
                offerEl.querySelector('.offers-offer-price').textContent = offer.price

                Array.from(offerEl.childNodes).forEach(node => offersTable.appendChild(node));
            })

            // empties table from old data
            while (articleContainer.firstChild)
                articleContainer.removeChild(articleContainer.firstChild)

            auction.base.articles.forEach(article => {
                const articleEl = articleTemplate.cloneNode(true)
                articleEl.querySelector('.offers-article-image').src = "data:image/jpeg;base64," + article.immagine
                articleEl.querySelector('.offers-article-name').textContent = article.name
                articleEl.querySelector('.offers-article-desc').textContent = article.description
                articleEl.querySelector('.offers-article-code').textContent = article.codArticle

                Array.from(articleEl.childNodes).forEach(node => articleContainer.appendChild(node));
            })
        }

        this.unmount = async () => {
            // hides everything that is being unloaded
            offerErrorQuery.setAttribute("hidden", "")
            offerErrorMax.setAttribute("hidden", "")
            offerLowPrice.setAttribute("hidden", "")
            offersContent.setAttribute("hidden", "")
        }

        this.mutateState = async (id) => {
            await this.unmount()
            await this.mount(new URLSearchParams({
                id: id || currentId
            }))
        }
    }
});