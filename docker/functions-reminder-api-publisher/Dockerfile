FROM functions-node-14


RUN git clone https://github.com/pagopa/io-functions-reminder-api-publisher.git /usr/src/app && \
    git checkout master && \
    yarn install --frozen-lockfile && \
    yarn build && \
    func extensions install

CMD ["func", "start", "--javascript"]