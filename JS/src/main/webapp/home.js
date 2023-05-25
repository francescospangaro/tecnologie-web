document.addEventListener('DOMContentLoaded', async () => {
    const pages = await Promise.all((() => {
        return [
            {id: "home", displayName: "Home", div: document.getElementById("home-page")},
            {id: "buy", displayName: "Buy", div: document.getElementById("buy-page"), view: buyPage},
            {id: "sell", displayName: "Sell", div: document.getElementById("sell-page")},
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
            set: async (newPage) => {
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
                        await newPage.view.mount()
                    } catch (e) {
                        console.error("Failed to mount new page", newPage, e)
                    }
                selectedPage = newPage;
            },
        }
        obj.setById = async (id) => {
            const newPage = pages.filter(p => p.id === id)
            if (!newPage)
                throw "invalid page id " + id
            await obj.set(newPage);
        }
        // Trigger the initial page mount
        obj.set(selectedPage)
        return obj
    })()

    const pagesMenu = document.getElementById('pages-menu')
    const pageLinkTemplate = document.getElementById('page-link-template')
    pages.forEach(page => {
        const pageLink = pageLinkTemplate.cloneNode(true)
        /** @type HTMLElement */
        const anchor = pageLink.querySelector('.link-anchor')
        anchor.textContent = page.displayName
        anchor.addEventListener('click', () => selectedPage.set(page));
        Array.from(pageLink.childNodes).forEach(node => {
            console.log(node)
            console.log(pagesMenu)
            pagesMenu.appendChild(node)
        })
    });

    const auctionRepository = (() => {
        const url = "http://localhost:8081/JS/"

        /**
         * @typedef {{ error: true, msg: string }} ErrorResponse
         * @typedef {{ error: false,  offerId: number, userId: number, auctionId: number, price: number, name: string, date: Date}} Offer
         * @typedef {{error: false, codArticle: number, name: string, description: string, immagine: string, prezzo: number, idUtente: number }} Article
         * @typedef {{error: false, id: number, expiry: Date, articles: Article[], minimumOfferDifference: number, maxOffer: number }} Auction
         * @typedef {{error: false, base: Auction, finalPrice: number, buyerName: string, buyerAddress: string }} ClosedAuction
         * @typedef {{error: false, base: Auction, offers: Offer[] }} OpenAuction
         */

        /**
         * @param {number} id
         * @return {Promise<ErrorResponse | Auction>} result
         */
        const getAuctionByIds = async function (id) {
            const response = await fetch(url + 'auctionDetails?id=' + id)
            /** @type {ErrorResponse | Auction} */
            const obj = await response.json();
            if (!obj.error)
                obj.map(a => {
                    a.expiry = new Date(a.expiry)
                    return a
                })
            return obj
        }

        const insertAuction = async function (formData) {
            const response = await fetch(url + 'insertAuction', {
                method: 'POST',
                body: formData,
            })

            const res = await response.json();
            if(!res.error)

            return await response.json();
        }

        /**
         * @param {string} keyWord
         * @returns {Promise<ErrorResponse | { error: false } & Auction[]>}
         */
        const searchAuction = async function (keyWord) {
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
         * @returns {Promise<ErrorResponse | {error: false} & Auction[]>}
         */
        const getBoughtAuctions = async function () {
            const response = await fetch(url + "auction")
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

        return {
            getAuctionByIds: getAuctionByIds,
            insertAuction: insertAuction,
            searchAuction: searchAuction,
            getBoughtAuctions: getBoughtAuctions
        }
    })();

    function buyPage(containerDiv) {

        const auctionTemplate = document.getElementById('found-auction-template')
        const articleTemplate = document.getElementById('article-template')

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

            if(keyword.value.trim() !== "") {
                const auctions = await auctionRepository.searchAuction(keyword.value) // Load from repo
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
            }
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
});