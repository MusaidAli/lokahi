import { makeExecutableSchema } from '@graphql-tools/schema'
import typeDefs from './typeDefs.js'
import { user, users } from './Queries/data.js'

const resolvers = {
  Query: {
    foo: () => user,
    bar: () => users
  },
  Mutation: {
    saveRoutingKey: (_, { key }) => key
  }
}

const schema = makeExecutableSchema({
  resolvers: [resolvers],
  typeDefs: [typeDefs]
})

export default {
  schema
}
