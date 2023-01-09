import { mount } from '@vue/test-utils'
import { TimeUnit } from '@/types'
import { Chip } from '@/types/metric'
import MetricChipList from '@/components/Inventory/MetricChipList.vue'

const metrics: Chip[] = [
  {
    type: 'latency',
    label: 'Latency',
    value: -1667930274660,
    timeUnit: TimeUnit.MSecs,
    status: 'UP'
  },
  {
    type: 'uptime',
    label: 'Uptime',
    value: 1667930274.66,
    timeUnit: TimeUnit.Secs,
    status: 'DOWN'
  },
  {
    type: 'status',
    label: 'Status',
    status: 'DOWN'
  }
]

let wrapper: any

// TODO: will fix after demo
describe.skip('Metric chip list', () => {
  beforeAll(() => {
    wrapper = mount(MetricChipList, {
      props: {
        metrics
      }
    })
  })
  afterAll(() => {
    wrapper.unmount()
  })

  it(`should have ${metrics.length} metric chip(s)`, () => {
    const chipItem = wrapper.findAllComponents('[data-test="metric-chip"]')
    expect(chipItem.length).toEqual(metrics.length)
  })
})
